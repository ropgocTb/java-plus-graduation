package ru.yandex.practicum.analyzer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String server;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffset;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;

    @Value("${spring.kafka.user-actions.value-deserializer}")
    private String userActionsValueDeserializer;

    @Value("${spring.kafka.user-actions.group-id}")
    private String userActionsGroupId;

    @Value("${spring.kafka.user-actions.client-id}")
    private String userActionsClientId;

    @Value("${spring.kafka.event-similarity.value-deserializer}")
    private String similarityValueDeserializer;

    @Value("${spring.kafka.event-similarity.group-id}")
    private String similarityGroupId;

    @Value("${spring.kafka.event-similarity.client-id}")
    private String similarityClientId;

    @Bean
    public KafkaConsumer<String, UserActionAvro> getUserActionsConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, userActionsValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, userActionsGroupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, userActionsClientId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        return new KafkaConsumer<>(properties);
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> getEventSimilarityConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, similarityValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, similarityGroupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, similarityClientId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        return new KafkaConsumer<>(properties);
    }
}
