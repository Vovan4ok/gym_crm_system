package org.volodymyrzganiaiko.workload_service.rpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.util.NoSuchElementException;

/**
 * gRPC endpoint for trainer workload queries: the gRPC counterpart of
 * {@code WorkloadController}. Spring gRPC finds this bean (a
 * {@link io.grpc.BindableService}) and registers it on the gRPC server.
 *
 * <p>The call is handled without blocking: the service's {@code Mono} is
 * subscribed to and its result is pushed into the {@link StreamObserver}.
 */
@Slf4j
@Service
public class WorkloadGrpcService extends WorkloadServiceGrpc.WorkloadServiceImplBase {
    private final WorkloadService workloadService;
    private final WorkloadProtoMapper mapper;

    public WorkloadGrpcService(WorkloadService workloadService, WorkloadProtoMapper mapper) {
        this.workloadService = workloadService;
        this.mapper = mapper;
    }

    @Override
    public void getWorkload(GetWorkloadRequest request, StreamObserver<GetWorkloadResponse> responseObserver) {
        String username = request.getTrainerUsername();
        if (username.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("trainer_username must not be blank")
                    .asRuntimeException());
            return;
        }
        log.info("gRPC GetWorkload for {}", username);

        // If the client cancels or its deadline expires, stop the Mongo query.
        // Registered before subscribing, because the Mono may complete immediately.
        Disposable.Swap subscription = Disposables.swap();
        if (responseObserver instanceof ServerCallStreamObserver<GetWorkloadResponse> serverObserver) {
            serverObserver.setOnCancelHandler(subscription::dispose);
        }

        subscription.update(workloadService.getWorkload(username)
                .map(mapper::toProto)
                .subscribe(
                        responseObserver::onNext,
                        error -> responseObserver.onError(toStatus(error, username)),
                        responseObserver::onCompleted));
    }

    private StatusRuntimeException toStatus(Throwable error, String username) {
        if (error instanceof NoSuchElementException) {
            log.info("gRPC GetWorkload: no workload for {}", username);
            return Status.NOT_FOUND.withDescription(error.getMessage()).asRuntimeException();
        }
        // Log the details here; never leak internals to the caller.
        log.error("gRPC GetWorkload failed for {}", username, error);
        return Status.INTERNAL.withDescription("Internal error").asRuntimeException();
    }
}
