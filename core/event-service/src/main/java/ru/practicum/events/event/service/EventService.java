package ru.practicum.events.event.service;

import ru.practicum.events.event.dto.*;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;

import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(SearchParamsAdmin searchParamsAdmin);

    List<EventShortDto> getEvents(SearchParams searchParams);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getEvent(Long id, Long userId);

    EventFullDto getEventNoHit(Long id);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    boolean existsByIdAndInitiator(Long eventId, Long initiatorId);

    void likeEvent(Long eventId, Long userId);

    List<EventShortDto> getRecommendations(Long userId, Integer size);
}
