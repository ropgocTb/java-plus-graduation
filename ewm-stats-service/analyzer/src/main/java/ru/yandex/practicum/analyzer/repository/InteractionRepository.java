package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findAllByUserId(Long userId);

    List<Interaction> findAllByEventIdIn(List<Long> eventIds);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long EventId);

    List<Interaction> findAllByUserIdOrderByTimestampDesc(Long userId, PageRequest pageRequest);
}
