package ru.practicum.main.event.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.model.Event;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryMapper categoryMapper;

    public EventShortDto mapToEventShortDto(Event event) {
        UserShortDto userShortDto = UserShortDto.builder()
                .id(event.getInitiator())
                .build();

        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryMapper.mapToResponseDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userShortDto)
                .views(event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public EventFullDto mapToEventFullDto(Event event) {
        UserShortDto userShortDto = UserShortDto.builder()
                .id(event.getInitiator())
                .build();

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(categoryMapper.mapToResponseDto(event.getCategory()))
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .initiator(userShortDto)
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .location(event.getLocation())
                .requestModeration(event.getRequestedModeration())
                .views(event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    public List<EventFullDto> mapToEventFullDtoList(List<Event> eventList) {
        return eventList.stream().map(this::mapToEventFullDto).toList();
    }

    public List<EventShortDto> mapToEventShortDtoList(List<Event> eventList) {
        return eventList.stream().map(this::mapToEventShortDto).toList();
    }
}
