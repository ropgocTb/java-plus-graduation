package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    Optional<Similarity> findByEventAAndEventB(Long eventA, Long eventB);

    List<Similarity> findAllByEventAOrEventB(Long eventA, Long eventB);
}
