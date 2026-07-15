package ru.practicum.events.event.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.category.model.Category;
import ru.practicum.events.category.repository.CategoryRepository;
import ru.practicum.events.event.dto.*;
import ru.practicum.events.event.mapper.EventMapper;
import ru.practicum.events.event.model.Event;
import ru.practicum.events.event.repository.EventRepository;
import ru.practicum.interaction.contract.request.RequestClient;
import ru.practicum.interaction.contract.user.PublicUserClient;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.EventState;
import ru.practicum.interaction.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.exception.BadRequestException;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.util.PaginationUtil;
import ru.practicum.stats.client.StatsAnalyzerClient;
import ru.practicum.stats.client.StatsCollectorClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ru.practicum.events.event.repository.EventRepository.Specs.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestClient requestClient;
    private final PublicUserClient userClient;

    private final StatsCollectorClient collectorClient;
    private final StatsAnalyzerClient analyzerClient;

    public EventServiceImpl(StatsCollectorClient collectorClient,
                            StatsAnalyzerClient analyzerClient,
                            EventRepository eventRepository,
                            CategoryRepository categoryRepository,
                            RequestClient requestClient,
                            PublicUserClient userClient,
                            EventMapper eventMapper) {
        this.collectorClient = collectorClient;
        this.analyzerClient = analyzerClient;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.requestClient = requestClient;
        this.userClient = userClient;
        this.eventMapper = eventMapper;
    }

    @Override
    public List<EventFullDto> getEvents(SearchParamsAdmin searchParamsAdmin) {

        Pageable pageRequest = PaginationUtil.createPageRequest(searchParamsAdmin.getFrom(), searchParamsAdmin.getSize());

        List<Event> events;

        Specification<Event> spec = Specification.where(null);

        if (searchParamsAdmin.getUsers() != null && !searchParamsAdmin.getUsers().isEmpty()) {
            spec = spec.and(inInitiators(searchParamsAdmin.getUsers()));
        }

        if (searchParamsAdmin.getStates() != null && !searchParamsAdmin.getStates().isEmpty()) {
            spec = spec.and(inStates(searchParamsAdmin.getStates()));
        }

        if (searchParamsAdmin.getCategories() != null && !searchParamsAdmin.getCategories().isEmpty()) {
            spec = spec.and(inCategories(searchParamsAdmin.getCategories()));
        }

        if (searchParamsAdmin.getRangeStart() != null && searchParamsAdmin.getRangeEnd() != null) {
            spec = spec.and(betweenTime(searchParamsAdmin.getRangeStart(), searchParamsAdmin.getRangeEnd()));
        } else {
            spec = spec.and(afterNow());
        }

        events = eventRepository.findAll(spec, pageRequest).getContent();

        enrichEvents(events);

        return eventMapper.mapToEventFullDtoList(events);
    }

    @Override
    public List<EventShortDto> getEvents(SearchParams searchParams) {
        Sort sort = getSort(searchParams.getSort());

        Pageable pageRequest = PaginationUtil.createPageRequestSorted(searchParams.getFrom(),
                searchParams.getSize(), sort);

        List<Event> events;

        Specification<Event> specification = Specification.where(byState(EventState.PUBLISHED));

        if (searchParams.getText() != null) {
            specification = specification.and(byAnnotationOrDescription(searchParams.getText()));
        }
        if (searchParams.getPaid() != null) {
            specification = specification.and(byPaid(searchParams.getPaid()));
        }
        if (searchParams.getCategories() != null && !searchParams.getCategories().isEmpty()) {
            specification = specification.and(inCategories(searchParams.getCategories()));
        }
        if (searchParams.getRangeStart() != null && searchParams.getRangeEnd() != null) {
            specification = specification.and(betweenTime(searchParams.getRangeStart(), searchParams.getRangeEnd()));
        } else {
            specification = specification.and(afterNow());
        }

        events = eventRepository.findAll(specification, pageRequest).getContent();

        enrichEvents(events);

        if (searchParams.getOnlyAvailable() != null && searchParams.getOnlyAvailable()) {
            List<Event> availableEvents = events.stream()
                    .filter(e -> e.getParticipantLimit() > e.getConfirmedRequests())
                    .toList();

            return eventMapper.mapToEventShortDtoList(availableEvents);
        }

        return eventMapper.mapToEventShortDtoList(events);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageRequest = PaginationUtil.createPageRequest(from, size);

        List<Event> events = eventRepository.findAllByInitiator(userId, pageRequest);

        enrichEvents(events);

        return eventMapper.mapToEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEvent(Long id, Long userId) {
        Event event = eventRepository.findByIdAndStateIs(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Public event with id " + id + " not found"));

        enrichEvent(event);

        collectorClient.sendView(id, userId);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto getEventNoHit(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Public event with id " + id + " not found"));

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiator(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " and initiator id " + userId +
                        " not found"));

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id " + newEventDto.getCategory() + " not found"));

        UserDto user;
        try {
            user = userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("No user found for id " + userId);
        }


        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2)))
            throw new BadRequestException("Event date cannot be earlier than two hours from now");

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .title(newEventDto.getTitle())
                .eventDate(newEventDto.getEventDate())
                .description(newEventDto.getDescription())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestedModeration(newEventDto.getRequestModeration())
                .category(category)
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .initiator(user.getId())
                .build();

        log.info("Creating event: {}", event);

        return eventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (!event.getState().equals(EventState.PENDING))
            throw new ConflictException("Event can only be published or rejected if it is in the pending state");

        if (updateEventAdminRequest.getAnnotation() != null)
            event.setAnnotation(updateEventAdminRequest.getAnnotation());

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventAdminRequest.getCategory() +
                            " not found")));
        }

        if (updateEventAdminRequest.getDescription() != null)
            event.setDescription(updateEventAdminRequest.getDescription());

        if (updateEventAdminRequest.getEventDate() != null) {

            if (updateEventAdminRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(1)))
                event.setEventDate(updateEventAdminRequest.getEventDate());
            else
                throw new BadRequestException("Event date cannot be earlier than one hour from now");

        } else if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Event date cannot be earlier than one hour from now");
        }

        if (updateEventAdminRequest.getLocation() != null)
            event.setLocation(updateEventAdminRequest.getLocation());

        if (updateEventAdminRequest.getPaid() != null)
            event.setPaid(updateEventAdminRequest.getPaid());

        if (updateEventAdminRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());

        if (updateEventAdminRequest.getRequestModeration() != null)
            event.setRequestedModeration(updateEventAdminRequest.getRequestModeration());

        if (updateEventAdminRequest.getTitle() != null)
            event.setTitle(updateEventAdminRequest.getTitle());

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT: {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {

        UserDto user;
        try {
            user = userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("No user found for id " + userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (!Objects.equals(event.getInitiator(), user.getId()))
            throw new ConflictException("User cannot update event that is not initiated by them");

        if (event.getState().equals(EventState.PUBLISHED))
            throw new ConflictException("Event cannot be updated once published");

        if (updateEventUserRequest.getAnnotation() != null)
            event.setAnnotation(updateEventUserRequest.getAnnotation());

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id " + updateEventUserRequest.getCategory() +
                            " not found")));
        }

        if (updateEventUserRequest.getDescription() != null)
            event.setDescription(updateEventUserRequest.getDescription());

        if (updateEventUserRequest.getEventDate() != null) {

            if (updateEventUserRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2)))
                event.setEventDate(updateEventUserRequest.getEventDate());
            else
                throw new BadRequestException("Event date cannot be earlier than two hours from now");

        } else if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date cannot be earlier than two hours from now");
        }

        if (updateEventUserRequest.getLocation() != null)
            event.setLocation(updateEventUserRequest.getLocation());

        if (updateEventUserRequest.getPaid() != null)
            event.setPaid(updateEventUserRequest.getPaid());

        if (updateEventUserRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());

        if (updateEventUserRequest.getRequestModeration() != null)
            event.setRequestedModeration(updateEventUserRequest.getRequestModeration());

        if (updateEventUserRequest.getTitle() != null)
            event.setTitle(updateEventUserRequest.getTitle());

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {
        if (!requestClient.hasConfirmedRequest(userId, eventId))
            throw new BadRequestException("User " + userId + " has no confirmed requests for event " + eventId);

        collectorClient.sendLike(userId, eventId);
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, Integer size) {
        List<Long> recommendedEventIds = analyzerClient.getRecommendations(userId, size);

        if (recommendedEventIds.isEmpty())
            return List.of();

        List<Event> recommendedEvents = eventRepository.findAllById(recommendedEventIds);

        enrichEvents(recommendedEvents);

        return eventMapper.mapToEventShortDtoList(recommendedEvents);
    }

    private Sort getSort(String sort) {
        String sortBy;

        if (sort == null) {
            sortBy = "id";
        } else if (sort.equals("EVENT_DATE")) {
            sortBy = "eventDate";
            return Sort.by(sortBy).ascending();
        } else if (sort.equals("VIEWS")) {
            sortBy = "views";
            return Sort.by(sortBy).descending();
        } else {
            sortBy = "id";
        }

        return Sort.by(sortBy).ascending();
    }

    @Override
    public boolean existsByIdAndInitiator(Long eventId, Long initiatorId) {
        return eventRepository.existsByIdAndInitiator(eventId, initiatorId);
    }

    private void enrichEvent(Event event) {
        event.setConfirmedRequests(requestClient.getConfirmedRequests(event.getId()).size());
        double rating = analyzerClient.getEventRating(event.getId());
        event.setRating(rating);
    }

    private void enrichEvents(List<Event> events) {
        if (events == null || events.isEmpty())
            return;

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<ParticipationRequestDto> requests = requestClient.getConfirmedRequestsForEvents(eventIds);

        Map<Long, Double> ratingMap = analyzerClient.getEventsRating(eventIds);
        Map<Long, Integer> requestsMap = new HashMap<>();

        for (ParticipationRequestDto request : requests) {
            Long eventId = request.getEvent();
            requestsMap.put(eventId, requestsMap.getOrDefault(eventId, 0) + 1);
        }

        for (Event event : events) {
            event.setRating(ratingMap.getOrDefault(event.getId(), 0.0));
            event.setConfirmedRequests(requestsMap.getOrDefault(event.getId(), 0));
        }
    }
}
