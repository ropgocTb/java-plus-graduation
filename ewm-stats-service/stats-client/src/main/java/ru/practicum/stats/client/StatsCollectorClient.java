package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import stats.service.collector.ActionTypeProto;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;

@Component
public class StatsCollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public void sendView(Long userId, Long eventId) {
        sendAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendRegister(Long userId, Long eventId) {
        sendAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendLike(Long userId, Long eventId) {
        sendAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void sendAction(Long userId, Long eventId, ActionTypeProto actionType) {
        long millis = System.currentTimeMillis();

        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1_000_000))
                .build();

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(timestamp)
                .build();

        collectorClient.collectUserAction(request);
    }
}
