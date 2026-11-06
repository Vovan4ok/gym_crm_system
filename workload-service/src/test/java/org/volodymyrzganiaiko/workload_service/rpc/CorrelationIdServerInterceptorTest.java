package org.volodymyrzganiaiko.workload_service.rpc;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class CorrelationIdServerInterceptorTest {
    private static final Metadata.Key<String> KEY = CorrelationIdServerInterceptor.CORRELATION_ID;

    /** What the MDC held while the service method ran. */
    private final AtomicReference<String> idSeenByService = new AtomicReference<>();
    private final AtomicReference<Metadata> responseHeaders = new AtomicReference<>();
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    public void start() throws IOException {
        WorkloadServiceGrpc.WorkloadServiceImplBase service = new WorkloadServiceGrpc.WorkloadServiceImplBase() {
            @Override
            public void getWorkload(GetWorkloadRequest request, StreamObserver<GetWorkloadResponse> observer) {
                idSeenByService.set(MDC.get("correlationId"));
                observer.onNext(GetWorkloadResponse.getDefaultInstance());
                observer.onCompleted();
            }
        };
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).directExecutor()
                .addService(ServerInterceptors.intercept(service, new CorrelationIdServerInterceptor()))
                .build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
    }

    @AfterEach
    public void stop() {
        MDC.clear();
        channel.shutdownNow();
        server.shutdownNow();
    }

    private void call(Metadata requestHeaders) {
        WorkloadServiceGrpc.newBlockingStub(channel)
                .withInterceptors(
                        MetadataUtils.newAttachHeadersInterceptor(requestHeaders),
                        MetadataUtils.newCaptureMetadataInterceptor(responseHeaders, new AtomicReference<>()))
                .getWorkload(GetWorkloadRequest.getDefaultInstance());
    }

    @Test
    public void incomingId_isInTheMdcAndEchoed() {
        Metadata headers = new Metadata();
        headers.put(KEY, "abc-123");

        call(headers);

        assertEquals("abc-123", idSeenByService.get());
        assertEquals("abc-123", responseHeaders.get().get(KEY));
    }

    @Test
    public void missingId_isGeneratedAndEchoed() {
        call(new Metadata());

        String generated = idSeenByService.get();
        assertDoesNotThrow(() -> UUID.fromString(generated));
        assertEquals(generated, responseHeaders.get().get(KEY));
    }

    @Test
    public void blankId_isReplacedWithAGeneratedOne() {
        Metadata headers = new Metadata();
        headers.put(KEY, " ");

        call(headers);

        assertDoesNotThrow(() -> UUID.fromString(idSeenByService.get()));
    }

    @Test
    public void mdcIsRestoredAfterTheCall() {
        // directExecutor: the server code runs on this thread, so a leak would show here.
        MDC.put("correlationId", "outer");
        Metadata headers = new Metadata();
        headers.put(KEY, "abc-123");

        call(headers);

        assertEquals("abc-123", idSeenByService.get());
        assertEquals("outer", MDC.get("correlationId"));
    }

    @Test
    public void noIdLeaksIntoAnEmptyMdc() {
        call(new Metadata());

        assertNull(MDC.get("correlationId"));
    }
}
