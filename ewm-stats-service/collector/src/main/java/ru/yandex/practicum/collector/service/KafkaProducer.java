package ru.yandex.practicum.collector.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
public class KafkaProducer {

    private final String userActionTopic;
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    public KafkaProducer(
            @Value("${stats.user-actions.topic}")
            String userActionTopic,
            KafkaTemplate<String, SpecificRecordBase> kafkaTemplate
    ) {
        this.userActionTopic = userActionTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(UserActionAvro userActionAvro) {
        kafkaTemplate.send(userActionTopic, userActionAvro);
    }
}
