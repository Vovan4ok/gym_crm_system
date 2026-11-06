package org.volodymyrzganiaiko.workload_service.rpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.workload_service.dto.MonthlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.YearlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.grpc.CalendarMonth;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Real gRPC calls through the in-process transport (no network port): checks
 * the wire-level behaviour a client sees, including status codes.
 */
@ExtendWith(MockitoExtension.class)
public class WorkloadGrpcServiceTest {
    @Mock
    private WorkloadService workloadService;

    private Server server;
    private ManagedChannel channel;
    private WorkloadServiceGrpc.WorkloadServiceBlockingStub stub;

    @BeforeEach
    public void startServer() throws IOException {
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .directExecutor()
                .addService(new WorkloadGrpcService(workloadService, new WorkloadProtoMapper()))
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        stub = WorkloadServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void stopServer() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    private static GetWorkloadRequest request(String username) {
        return GetWorkloadRequest.newBuilder().setTrainerUsername(username).build();
    }

    @Test
    public void getWorkload_existingTrainer_returnsSummary() {
        TrainerSummaryResponse summary = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", true,
                List.of(new YearlySummaryResponse(2026, List.of(new MonthlySummaryResponse(Month.JULY, 90)))));
        when(workloadService.getWorkload("Tra.Iner")).thenReturn(Mono.just(summary));

        GetWorkloadResponse response = stub.getWorkload(request("Tra.Iner"));

        assertEquals("Tra.Iner", response.getTrainerUsername());
        assertEquals("Tra", response.getFirstName());
        assertTrue(response.getActive());
        assertEquals(2026, response.getYears(0).getYear());
        assertEquals(CalendarMonth.MONTH_JULY, response.getYears(0).getMonths(0).getMonth());
        assertEquals(90, response.getYears(0).getMonths(0).getSummaryDuration());
    }

    @Test
    public void getWorkload_unknownTrainer_returnsNotFound() {
        when(workloadService.getWorkload("No.One"))
                .thenReturn(Mono.error(new NoSuchElementException("no workload for No.One")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getWorkload(request("No.One")));

        assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
        assertEquals("no workload for No.One", ex.getStatus().getDescription());
    }

    @Test
    public void getWorkload_blankUsername_returnsInvalidArgument() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getWorkload(request(" ")));

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
        verifyNoInteractions(workloadService);
    }

    @Test
    public void getWorkload_unexpectedError_returnsInternalWithoutDetails() {
        when(workloadService.getWorkload("Tra.Iner"))
                .thenReturn(Mono.error(new IllegalStateException("mongo password=secret")));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.getWorkload(request("Tra.Iner")));

        assertEquals(Status.Code.INTERNAL, ex.getStatus().getCode());
        assertEquals("Internal error", ex.getStatus().getDescription());
    }

    @Test
    public void getWorkload_clientDeadlineExpires_cancelsTheQuery() {
        AtomicBoolean queryCancelled = new AtomicBoolean();
        when(workloadService.getWorkload("Tra.Iner"))
                .thenReturn(Mono.<TrainerSummaryResponse>never().doOnCancel(() -> queryCancelled.set(true)));

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> stub.withDeadlineAfter(200, TimeUnit.MILLISECONDS).getWorkload(request("Tra.Iner")));

        assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
        await().atMost(Duration.ofSeconds(2)).untilTrue(queryCancelled);
    }
}
