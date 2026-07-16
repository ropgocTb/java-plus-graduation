package ru.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.stats.service.dashboard.InteractionsCountRequestProto;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc;
import ru.practicum.stats.service.dashboard.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatsAnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    public double getEventRating(Long eventId) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addEventIds(eventId)
                .build();

        return analyzerClient.getInteractionsCount(request).hasNext() ?
                analyzerClient.getInteractionsCount(request).next().getScore() : 0.0;
    }

    public Map<Long, Double> getEventsRating(List<Long> eventIds) {
        Map<Long, Double> response = new HashMap<>();

        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventIds(eventIds)
                .build();

        analyzerClient.getInteractionsCount(request)
                .forEachRemaining(eventProto ->
                        response.put(eventProto.getEventId(), eventProto.getScore()));

        return response;
    }

    public List<Long> getRecommendations(Long userId, int maxResults) {
        List<Long> response = new ArrayList<>();

        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        analyzerClient.getRecommendationsForUser(request)
                .forEachRemaining(eventProto -> response.add(eventProto.getEventId()));

        return response;
    }
}
