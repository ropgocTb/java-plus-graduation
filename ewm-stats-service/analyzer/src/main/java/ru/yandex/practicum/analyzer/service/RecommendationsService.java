package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.model.Interaction;
import ru.yandex.practicum.analyzer.model.Similarity;
import ru.yandex.practicum.analyzer.repository.InteractionRepository;
import ru.yandex.practicum.analyzer.repository.SimilarityRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationsService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    private static final int NEIGHBOURS = 5;

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        return interactionRepository.findAllByEventIdIn(eventIds)
                .stream()
                .collect(Collectors.groupingBy(
                        Interaction::getEventId,
                        Collectors.summingDouble(Interaction::getRating)
                ));
    }

    public void saveSimilarity(EventSimilarityAvro event) {
        Similarity similarity = similarityRepository.findByEventAAndEventB(event.getEventA(), event.getEventB())
                .orElse(Similarity.builder()
                        .eventA(event.getEventA())
                        .eventB(event.getEventB())
                        .build());

        similarity.setSimilarity(event.getScore());
        similarity.setTimestamp(event.getTimestamp());
        log.info("saving similarity a b: {}, {}", similarity.getEventA(), similarity.getEventB());
        similarityRepository.save(similarity);
    }

    public void saveInteraction(UserActionAvro action) {
        long userId = action.getUserId();
        long eventId = action.getEventId();
        double rating = getInteractionWeight(action.getActionType());

        Optional<Interaction> interactionOpt = interactionRepository.findByUserIdAndEventId(userId, eventId);

        if (interactionOpt.isEmpty()) {
            Interaction newInteraction = Interaction.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .rating(rating)
                    .timestamp(action.getTimestamp())
                    .build();
            interactionRepository.save(newInteraction);
            return;
        }

        Interaction interaction = interactionOpt.get();

        if (interaction.getRating() >= rating) return;

        interaction.setRating(rating);
        interaction.setTimestamp(action.getTimestamp());
        interactionRepository.save(interaction);
    }

    public List<Map.Entry<Long,Double>> getRecommendations(long userId, int maxResults) {
        List<Interaction> recent = interactionRepository.findAllByUserIdOrderByTimestampDesc(userId);

        if (recent.isEmpty()) return List.of();

        Set<Long> watched = recent
                .stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        Set<Long> candidates = new HashSet<>();

        for (Interaction interaction : recent) {
            List<Similarity> similarities = similarityRepository.findAllByEventAOrEventB(
                    interaction.getEventId(),
                    interaction.getEventId()
            );

            similarities.forEach(s -> {
                long event = s.getEventA().equals(interaction.getEventId())
                        ? s.getEventB()
                        : s.getEventA();

                if(!watched.contains(event)) candidates.add(event);
            });
        }

        return candidates.stream()
                .map(event -> Map.entry(event, predict(userId, event)))
                .sorted(Map.Entry.<Long,Double> comparingByValue().reversed())
                .limit(maxResults)
                .toList();
    }

    private double predict(long userId, long targetEvent) {
        List<Interaction> userRatings = interactionRepository.findAllByUserId(userId);
        List<Neighbour> neighbours = new ArrayList<>();

        for (Interaction interaction : userRatings) {
            Similarity similarity = getSimilarity(interaction.getEventId(), targetEvent);
            if(similarity!=null) neighbours.add(new Neighbour(interaction.getRating(), similarity.getSimilarity()));
        }

        neighbours.sort(Comparator.comparing(Neighbour::similarity).reversed());
        neighbours = neighbours.stream()
                .limit(NEIGHBOURS)
                .toList();

        double numerator = 0;
        double denominator = 0;

        for (Neighbour neighbour : neighbours) {
            numerator += neighbour.rating() * neighbour.similarity();
            denominator += neighbour.similarity();
        }

        if (denominator == 0) return 0;
        return numerator / denominator;
    }

    private Similarity getSimilarity(long first, long second){
        return similarityRepository.findByEventAAndEventB(Math.min(first,second), Math.max(first,second))
                .orElse(null);
    }

    private record Neighbour(double rating, double similarity) {}

    public List<Map.Entry<Long, Double>> getSimilarEvents(long eventId, long userId, int maxResults) {
        Set<Long> viewedEvents = interactionRepository.findAllByUserId(userId)
                .stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        return similarityRepository.findAllByEventAOrEventB(eventId, eventId).stream()
                .map(s -> Map.entry(getEventOrder(s, eventId), s.getSimilarity()))
                .filter(e -> !viewedEvents.contains(e.getKey()))
                .sorted(Map.Entry.<Long,Double> comparingByValue() .reversed())
                .limit(maxResults)
                .toList();
    }

    private long getEventOrder(Similarity similarity, long eventId){
        return similarity.getEventA() == eventId ? similarity.getEventB() : similarity.getEventA();
    }

    private double getInteractionWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
