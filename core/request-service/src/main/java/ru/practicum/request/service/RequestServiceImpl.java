package ru.practicum.request.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.contract.event.PublicEventClient;
import ru.practicum.interaction.contract.user.PublicUserClient;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.request.RequestStatus;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestMapper requestMapper;
    private final RequestRepository requestRepository;
    private final PublicUserClient userClient;
    private final PublicEventClient eventClient;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        if (!userClient.existsById(userId))
            throw new NotFoundException("User with id " + userId + " not found");

        EventFullDto event;

        try {
            event = eventClient.getEventNoHit(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Event with id " + eventId + " not found");
        }


        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in an unpublished event");
        }

        if (requestRepository.existsByRequesterAndEvent(userId, eventId)) {
            throw new ConflictException("Duplicate participation request");
        }

        Integer limit = event.getParticipantLimit();
        boolean unlimited = (limit == null || limit == 0);

        if (!unlimited) {
            long confirmed = requestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED);

            if (confirmed >= limit) {
                throw new ConflictException("Participant limit reached");
            }
        }

        boolean moderationRequired = Boolean.TRUE.equals(event.getRequestModeration());

        RequestStatus status = (unlimited || !moderationRequired)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event.getId())
                .requester(userId)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        Request saved = requestRepository.save(request);

        return requestMapper.mapToParticipationRequestDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        List<Request> existingRequests = requestRepository.findAllByRequester(userId);
        return requestMapper.mapToListParticipationRequestDto(existingRequests);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        if (!request.getRequester().equals(userId)) {
            throw new ConflictException("User cannot cancel request of another user");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.mapToParticipationRequestDto(request);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId,
                                                                Long eventId,
                                                                EventRequestStatusUpdateRequest dto) {
        // Проверка на User
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        EventFullDto event;

        try {
            event = eventClient.getEventNoHit(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("No published event found for id " + eventId);
        }

        // Проверка на Initiator
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the initiator of the event");
        }

        List<Long> requestIds = dto.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }

        List<Request> requests = requestRepository.findAllById(requestIds);

        // Проверка на существование всех переданных запросов
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Some participation requests were not found");
        }

        // Проверка запросов, что они относятся к одному Event
        boolean hasForeignEvent = requests.stream()
                .anyMatch(r -> !r.getEvent().equals(eventId));
        if (hasForeignEvent) {
            throw new ConflictException("Some requests do not belong to event id=" + eventId);
        }

        // Менять статус можно только у PENDING
        boolean hasNotPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);
        if (hasNotPending) {
            throw new ConflictException("Only PENDING requests can be updated");
        }

        // Проверка статуса на допустимость
        RequestStatus targetStatus = dto.getStatus();
        if (targetStatus != RequestStatus.CONFIRMED && targetStatus != RequestStatus.REJECTED) {
            throw new ConflictException("Only CONFIRMED/REJECTED are allowed");
        }

        // REJECTED: отклоняем переданные заявки
        if (targetStatus == RequestStatus.REJECTED) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            List<Request> saved = requestRepository.saveAll(requests);

            List<ParticipationRequestDto> toResponse = requestMapper.mapToListParticipationRequestDto(saved);
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(toResponse)
                    .build();
        }

        // CONFIRMED: учитываем лимит
        Integer limit = event.getParticipantLimit();
        if (limit != null && limit > 0) {
            long alreadyConfirmed = requestRepository.countByEventAndStatus(eventId, RequestStatus.CONFIRMED);
            long available = limit - alreadyConfirmed;

            if (available <= 0) {
                throw new ConflictException("Participant limit reached");
            }

            List<Request> toConfirm = new ArrayList<>();
            List<Request> toReject = new ArrayList<>();

            for (Request r : requests) {
                if (available > 0) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    toConfirm.add(r);
                    available--;
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    toReject.add(r);
                }
            }

            requestRepository.saveAll(requests);

            // Отклоняем оставшиеся запросы
            long confirmedNow = toConfirm.size();
            long totalConfirmed = alreadyConfirmed + confirmedNow;
            if (totalConfirmed >= limit) {
                List<Request> pending = requestRepository.findAllByEventAndStatus(eventId, RequestStatus.PENDING);

                pending.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                requestRepository.saveAll(pending);
            }

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requestMapper.mapToListParticipationRequestDto(toConfirm))
                    .rejectedRequests(requestMapper.mapToListParticipationRequestDto(toReject))
                    .build();
        }

        // CONFIRMED: без лимита
        requests.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        List<Request> saved = requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestMapper.mapToListParticipationRequestDto(saved))
                .rejectedRequests(List.of())
                .build();
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!eventClient.existsByIdAndInitiator(eventId, userId)) {
            throw new ConflictException("User is not the initiator of the event");
        }

        List<Request> requests = requestRepository.findAllByEvent(eventId);
        return requestMapper.mapToListParticipationRequestDto(requests);
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequests(Long eventId) {
        List<Request> confirmedRequests = requestRepository.findAllByEventAndStatus(eventId, RequestStatus.CONFIRMED);
        return requestMapper.mapToListParticipationRequestDto(confirmedRequests);
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequestsForEvents(List<Long> eventIds) {
        List<Request> confirmedRequests = requestRepository.findAllByEventInAndStatus(eventIds, RequestStatus.CONFIRMED);
        return requestMapper.mapToListParticipationRequestDto(confirmedRequests);
    }
}
