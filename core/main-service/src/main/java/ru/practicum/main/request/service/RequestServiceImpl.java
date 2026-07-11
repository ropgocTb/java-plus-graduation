package ru.practicum.main.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.contract.user.PublicUserClient;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestMapper requestMapper;
    private final RequestRepository requestRepository;
    private final PublicUserClient userClient;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {

        UserDto user = userClient.getUserById(userId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getInitiator().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByRequesterAndEventId(userId, eventId)) {
            throw new ConflictException("Duplicate participation request");
        }

        Integer limit = event.getParticipantLimit();
        boolean unlimited = (limit == null || limit == 0);

        if (!unlimited) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

            if (confirmed >= limit) {
                throw new ConflictException("Participant limit reached");
            }
        }

        boolean moderationRequired = Boolean.TRUE.equals(event.getRequestedModeration());

        RequestStatus status = (unlimited || !moderationRequired)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        Request request = Request.builder()
                .event(event)
                .requester(user.getId())
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
}
