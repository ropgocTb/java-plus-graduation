package ru.practicum.interaction.contract.request;

import ru.practicum.interaction.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;

import java.util.List;

public class RequestFallback implements RequestClient {
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequests(Long eventId) {
        throw new RuntimeException("Request service is unavailable");
    }

    @Override
    public List<ParticipationRequestDto> getConfirmedRequestsForEvents(List<Long> eventIds) {
        throw new RuntimeException("Request service is unavailable");
    }
}
