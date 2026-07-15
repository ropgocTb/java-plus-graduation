package ru.yandex.practicum.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.collector.mapper.CollectorMapper;
import stats.service.collector.UserActionProto;

@Service
@Slf4j
public class CollectorService {

    @Value("stats.user-actions.topic")
    private String topic;

    private final KafkaProducer kafkaProducer;
    private final CollectorMapper collectorMapper;

    public CollectorService(KafkaProducer producer, CollectorMapper mapper) {
        this.kafkaProducer = producer;
        this.collectorMapper = mapper;
    }

    public void handle(UserActionProto userActionProto) {
        if (userActionProto == null)
            throw new IllegalArgumentException("null action");

        log.info("Trying to save userAction: {}, {}", userActionProto.getUserId(), userActionProto.getEventId());

        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(collectorMapper.mapActionTypeProtoToActionTypeAvro(userActionProto.getActionType()))
                .setTimestamp(collectorMapper.mapTimestampToInstant(userActionProto.getTimestamp()))
                .build();

        kafkaProducer.send(userActionAvro);
    }

}
