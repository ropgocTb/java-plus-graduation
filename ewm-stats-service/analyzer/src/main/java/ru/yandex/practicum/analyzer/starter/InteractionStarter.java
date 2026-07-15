package ru.yandex.practicum.analyzer.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.service.RecommendationsService;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InteractionStarter implements Runnable {

    @Value("${spring.kafka.user-actions.topic}")
    private String consumerTopic;

    private final Consumer<String, UserActionAvro> consumer;
    private final RecommendationsService service;

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(consumerTopic));

            log.info("Подписка на топик: {}", consumerTopic);

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    service.saveInteraction(record.value());
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception ex) {
            log.error("Ошибка во время обработки событий", ex);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
