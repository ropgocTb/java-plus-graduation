package ru.yandex.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.stats.service.dashboard.*;
import ru.yandex.practicum.analyzer.service.RecommendationsService;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService service;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> response) {
        try {
            service.getRecommendations(request.getUserId(), request.getMaxResults())
                    .forEach(entry -> response.onNext(
                    RecommendedEventProto.newBuilder()
                            .setEventId(entry.getKey())
                            .setScore(entry.getValue())
                            .build()
            ));

            response.onCompleted();
        } catch (Exception ex) {
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(ex.getLocalizedMessage())
                            .withCause(ex)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> response) {
        try {
            service.getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults())
                    .forEach(entry -> response.onNext(
                    RecommendedEventProto.newBuilder()
                            .setEventId(entry.getKey())
                            .setScore(entry.getValue())
                            .build()
            ));
            response.onCompleted();
        } catch (Exception ex) {
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(ex.getLocalizedMessage())
                            .withCause(ex)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> response) {
        try {
            service.getInteractionsCount(request.getEventIdsList()).forEach((eventId, score) ->
                response.onNext(RecommendedEventProto.newBuilder()
                                .setEventId(eventId)
                                .setScore(score)
                                .build())
            );
            response.onCompleted();
        } catch (Exception ex) {
            response.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(ex.getLocalizedMessage())
                            .withCause(ex)
            ));
        }
    }
}
