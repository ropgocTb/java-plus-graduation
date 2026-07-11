package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.main.event.dto.*;

import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(SearchParamsAdmin searchParamsAdmin);

    List<EventShortDto> getEvents(SearchParams searchParams, HttpServletRequest request);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getEvent(Long id, HttpServletRequest request);

    EventFullDto getEventNoHit(Long id);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    boolean existsByIdAndInitiator(Long eventId, Long initiatorId);
}
