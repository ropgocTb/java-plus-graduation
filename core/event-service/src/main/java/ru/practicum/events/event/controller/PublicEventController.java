package ru.practicum.events.event.controller;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.event.dto.SearchParams;
import ru.practicum.events.event.service.EventService;
import ru.practicum.interaction.contract.event.PublicEventOperations;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PublicEventController implements PublicEventOperations {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size,
                                         HttpServletRequest request) {
        log.info("Getting events: rangeStart={}, from={}, size={}", rangeStart, from, size);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }

        SearchParams searchParams = SearchParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        return eventService.getEvents(searchParams, request);
    }

    @GetMapping("/{id}")
    @Retry(name = "retryGet")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Getting event: id={}", id);
        return eventService.getEvent(id, request);
    }

    @Override
    public boolean existsByIdAndInitiator(Long eventId, Long userId) {
        log.info("Checking initiator for event: id={}", eventId);
        return eventService.existsByIdAndInitiator(eventId, userId);
    }

    @Override
    public EventFullDto getEventNoHit(@PathVariable Long id) {
        log.info("Getting event with no hit for stats: id={}", id);
        return eventService.getEventNoHit(id);
    }
}
