package ru.practicum.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.contract.request.RequestOperations;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController implements RequestOperations {

    private final RequestService requestService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                                 @RequestParam @Positive Long eventId) {
        log.info("Creating request: userId={}, eventId={}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("Getting user requests: userId={}", userId);
        return requestService.getUserRequests(userId);
    }

    @Override
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("Canceling request: userId={}", userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(@PathVariable @Positive Long userId,
                                                                @PathVariable @Positive Long eventId,
                                                                @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Updating request statuses: userId={}, eventId={}", userId, eventId);
        return requestService.updateRequestStatuses(userId, eventId, request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Getting event requests: userId={}, eventId={}", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequests(@PathVariable Long eventId) {
        log.info("Getting confirmed event requests: eventId={}", eventId);
        return requestService.getConfirmedRequests(eventId);
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds) {
        log.info("Getting confirmed event requests: eventsId={}", eventIds);
        return requestService.getConfirmedRequestsForEvents(eventIds);
    }

    @Override
    public boolean hasConfirmedRequest(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Getting confirmed request: userId={}, eventId={}", userId, eventId);
        return requestService.hasConfirmedRequest(userId, eventId);
    }
}
