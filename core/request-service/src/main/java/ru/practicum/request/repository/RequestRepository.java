package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.interaction.dto.request.RequestStatus;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterAndEvent(Long userId, Long eventId);

    long countByEventAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findAllByRequester(Long userId);

    List<Request> findAllByEventAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findAllByEvent(Long eventId);

    List<Request> findAllByEventInAndStatus(List<Long> eventIds, RequestStatus requestStatus);
}
