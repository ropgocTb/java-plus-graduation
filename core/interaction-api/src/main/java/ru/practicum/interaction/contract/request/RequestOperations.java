package ru.practicum.interaction.contract.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestOperations {

    @PostMapping("/{userId}/requests")
    ParticipationRequestDto createRequest(@PathVariable @Positive Long userId,
                                          @RequestParam @Positive Long eventId);

    @GetMapping("/{userId}/requests")
    List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long requestId);

    @PatchMapping("/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateRequestStatuses(@PathVariable @Positive Long userId,
                                                         @PathVariable @Positive Long eventId,
                                                         @RequestBody @Valid EventRequestStatusUpdateRequest request);

    @GetMapping("/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId);

    @GetMapping("/events/{eventId}/requests/confirmed")
    List<ParticipationRequestDto> getConfirmedRequests(@PathVariable Long eventId);

    @GetMapping("/events/requests/confirmed")
    List<ParticipationRequestDto> getConfirmedRequestsForEvents(@RequestParam List<Long> eventIds);

    @GetMapping("/{userId}/events/{eventId}/confirmed")
    boolean hasConfirmedRequest(@PathVariable Long userId, @PathVariable Long eventId);
}
