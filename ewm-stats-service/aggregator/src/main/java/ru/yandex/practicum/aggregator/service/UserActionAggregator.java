package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserActionAggregator {

    private final String producerTopic;

    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();
    private final Map<Long, Double> eventSums = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;
    private static final double DEFAULT_VALUE = 0.0;

    public UserActionAggregator(@Value("${stats.events-similarity.topic}") String producerTopic) {
        this.producerTopic = producerTopic;
    }

    public void processAction(UserActionAvro userActionAvro, Producer<String, EventSimilarityAvro> producer) {
        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        double weight = getWeight(userActionAvro.getActionType());

        Map<Long, Double> users = eventWeights.computeIfAbsent(eventId, id -> new ConcurrentHashMap<>());

        Double oldWeight = users.get(userId);
        if (oldWeight != null && oldWeight >= weight) return;

        users.put(userId, weight);
        updateEventSum(eventId, oldWeight, weight);
        recalculate(eventId, userId, oldWeight, weight, userActionAvro.getTimestamp(), producer);
    }

    private void updateEventSum(long eventId, Double oldWeight, double newWeight) {
        double delta = oldWeight == null ? newWeight : newWeight - oldWeight;
        eventSums.merge(eventId, delta, Double::sum);
    }

    private void recalculate(long changedEvent, long userId,
                             Double oldWeight, double newWeight,
                             Instant timestamp, Producer<String, EventSimilarityAvro> producer) {
        for (Map.Entry<Long, Map<Long, Double>> entry : eventWeights.entrySet()) {
            long anotherEvent = entry.getKey();
            if (anotherEvent == changedEvent) continue;

            Double anotherWeight = entry.getValue().get(userId);
            if (anotherWeight == null) continue;

            updateMinSum(changedEvent, anotherEvent, oldWeight, newWeight, anotherWeight);
            sendSimilarity(changedEvent, anotherEvent, timestamp, producer);
        }
    }

    private void updateMinSum(long eventA, long eventB, Double oldWeight, double newWeight, double secondWeight) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        double oldValue = oldWeight == null ? DEFAULT_VALUE : Math.min(oldWeight, secondWeight);
        double newValue = Math.min(newWeight, secondWeight);
        double delta = newValue - oldValue;

        minWeightsSums.computeIfAbsent(first, id -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }

    public void sendSimilarity(long eventA, long eventB, Instant timestamp,
                               Producer<String, EventSimilarityAvro> producer) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        double sMin = minWeightsSums.getOrDefault(first, Map.of()).getOrDefault(second, DEFAULT_VALUE);
        double sFirst = eventSums.getOrDefault(first, DEFAULT_VALUE);
        double sSecond = eventSums.getOrDefault(second, DEFAULT_VALUE);
        if (sFirst == 0 || sSecond == 0) return;

        double score = sMin / (Math.sqrt(sFirst) * Math.sqrt(sSecond));

        EventSimilarityAvro result = EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();

        log.info("Sending similarity for eventA: {}", result.getEventA());
        producer.send(new ProducerRecord<>(producerTopic, String.valueOf(result.getEventA()), result));
    }

    private double getWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }

}
