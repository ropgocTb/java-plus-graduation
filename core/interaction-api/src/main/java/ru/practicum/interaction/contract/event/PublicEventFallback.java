package ru.practicum.interaction.contract.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

public class PublicEventFallback implements PublicEventClient {
    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {
        throw new RuntimeException("Event service is unavailable");
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        throw new RuntimeException("Event service is unavailable");
    }

    @Override
    public EventFullDto getEventNoHit(Long id) {
        throw new RuntimeException("Event service is unavailable");
    }

    @Override
    public boolean existsByIdAndInitiator(Long eventId, Long userId) {
        return false;
    }
}
