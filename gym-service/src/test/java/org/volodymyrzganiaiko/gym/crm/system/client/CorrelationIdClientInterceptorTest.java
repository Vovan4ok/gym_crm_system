package org.volodymyrzganiaiko.gym.crm.system.client;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationIdClientInterceptorTest {
    private final AtomicReference<Metadata> receivedHeaders = new AtomicReference<>();
    private Server server;
    private ManagedChannel channel;
    private WorkloadServiceGrpc.WorkloadServiceBlockingStub stub;

    @BeforeEach
    public void start() throws IOException {
        WorkloadServiceGrpc.WorkloadServiceImplBase service = new WorkloadServiceGrpc.WorkloadServiceImplBase() {
            @Override
            public void getWorkload(GetWorkloadRequest request, StreamObserver<GetWorkloadResponse> observer) {
                observer.onNext(GetWorkloadResponse.getDefaultInstance());
                observer.onCompleted();
            }
        };
        // Server side records the metadata the client sent.
        ServerInterceptor recordHeaders = new ServerInterceptor() {
            @Override
            public <Q, R> io.grpc.ServerCall.Listener<Q> interceptCall(io.grpc.ServerCall<Q, R> call, Metadata headers,
                                                                      io.grpc.ServerCallHandler<Q, R> next) {
                receivedHeaders.set(headers);
                return next.startCall(call, headers);
            }
        };
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).directExecutor()
                .addService(ServerInterceptors.intercept(service, recordHeaders)).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        stub = WorkloadServiceGrpc.newBlockingStub(channel).withInterceptors(new CorrelationIdClientInterceptor());
    }

    @AfterEach
    public void stop() {
        MDC.clear();
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    public void sendsTheCorrelationIdFromTheMdc() {
        MDC.put("correlationId", "abc-123");

        stub.getWorkload(GetWorkloadRequest.getDefaultInstance());

        assertEquals("abc-123", receivedHeaders.get().get(CorrelationIdClientInterceptor.CORRELATION_ID));
    }

    @Test
    public void sendsNothingWhenThereIsNoCorrelationId() {
        stub.getWorkload(GetWorkloadRequest.getDefaultInstance());

        assertNull(receivedHeaders.get().get(CorrelationIdClientInterceptor.CORRELATION_ID));
    }
}
