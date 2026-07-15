package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.service.CollectorService;
import stats.service.collector.UserActionControllerGrpc;
import stats.service.collector.UserActionProto;

@GrpcService
@RequiredArgsConstructor
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> response) {
        try {
            collectorService.handle(request);
            response.onNext(Empty.getDefaultInstance());
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
