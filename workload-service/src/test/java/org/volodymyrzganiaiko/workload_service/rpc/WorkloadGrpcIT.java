package org.volodymyrzganiaiko.workload_service.rpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.lifecycle.GrpcServerStartedEvent;
import org.volodymyrzganiaiko.workload_service.AbstractMongoIT;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import org.volodymyrzganiaiko.workload_service.grpc.CalendarMonth;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;
import org.volodymyrzganiaiko.workload_service.repository.TrainerWorkloadRepository;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boots the whole application against a real MongoDB and calls the gRPC API
 * over a real Netty port: proves Spring gRPC registers the service and that
 * the call goes through to Mongo end to end.
 */
@SpringBootTest
public class WorkloadGrpcIT extends AbstractMongoIT {

    /** Captures the port the gRPC server actually bound (0 = random in tests). */
    @TestConfiguration
    static class GrpcPortCapture {
        static final AtomicInteger PORT = new AtomicInteger();

        @Bean
        ApplicationListener<GrpcServerStartedEvent> grpcPortListener() {
            return event -> PORT.set(event.getPort());
        }
    }

    @Autowired
    private TrainerWorkloadRepository repository;

    private ManagedChannel channel;
    private WorkloadServiceGrpc.WorkloadServiceBlockingStub stub;

    @BeforeEach
    public void connect() {
        channel = ManagedChannelBuilder.forAddress("localhost", GrpcPortCapture.PORT.get())
                .usePlaintext()
                .build();
        stub = WorkloadServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    @AfterEach
    public void cleanUp() {
        channel.shutdownNow();
        repository.deleteAll().block();
    }

    @Test
    public void getWorkload_storedTrainer_returnsSummaryOverTheNetwork() {
        repository.save(new TrainerWorkload("Grpc.Trainer", "Grpc", "Trainer", true,
                new ArrayList<>(List.of(new TrainerWorkload.YearSummary(2026,
                        new ArrayList<>(List.of(new TrainerWorkload.MonthSummary(Month.MAY, 120)))))))).block();

        GetWorkloadResponse response = stub.getWorkload(
                GetWorkloadRequest.newBuilder().setTrainerUsername("Grpc.Trainer").build());

        assertEquals("Grpc", response.getFirstName());
        assertEquals(CalendarMonth.MONTH_MAY, response.getYears(0).getMonths(0).getMonth());
        assertEquals(120, response.getYears(0).getMonths(0).getSummaryDuration());
    }

    @Test
    public void getWorkload_unknownTrainer_returnsNotFound() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () -> stub.getWorkload(
                GetWorkloadRequest.newBuilder().setTrainerUsername("Nobody.Here").build()));

        assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }
}
