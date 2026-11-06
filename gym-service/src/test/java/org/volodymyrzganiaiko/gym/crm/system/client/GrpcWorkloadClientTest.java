package org.volodymyrzganiaiko.gym.crm.system.client;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.CalendarMonth;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.MonthlySummary;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;
import org.volodymyrzganiaiko.workload_service.grpc.YearlySummary;

import java.io.IOException;
import java.time.Duration;
import java.time.Month;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the client against a fake workload-service on the in-process
 * transport: success, NOT_FOUND, failures, deadlines and contract drift.
 */
public class GrpcWorkloadClientTest {

    /** Fake server whose behaviour each test sets. */
    private static class FakeWorkloadService extends WorkloadServiceGrpc.WorkloadServiceImplBase {
        BiConsumer<GetWorkloadRequest, StreamObserver<GetWorkloadResponse>> behaviour;

        @Override
        public void getWorkload(GetWorkloadRequest request, StreamObserver<GetWorkloadResponse> observer) {
            behaviour.accept(request, observer);
        }
    }

    private final FakeWorkloadService fake = new FakeWorkloadService();
    private Server server;
    private ManagedChannel channel;

    @BeforeEach
    public void startServer() throws IOException {
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).directExecutor().addService(fake).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
    }

    @AfterEach
    public void stopServer() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    private GrpcWorkloadClient client(Duration deadline) {
        return new GrpcWorkloadClient(WorkloadServiceGrpc.newBlockingStub(channel), deadline);
    }

    private static void reply(StreamObserver<GetWorkloadResponse> observer, GetWorkloadResponse response) {
        observer.onNext(response);
        observer.onCompleted();
    }

    private static GetWorkloadResponse.Builder trainer() {
        return GetWorkloadResponse.newBuilder()
                .setTrainerUsername("Tra.Iner").setFirstName("Tra").setLastName("Iner").setActive(true);
    }

    @Test
    public void findWorkload_mapsTheResponse() {
        fake.behaviour = (request, observer) -> reply(observer, trainer()
                .addYears(YearlySummary.newBuilder().setYear(2026)
                        .addMonths(MonthlySummary.newBuilder()
                                .setMonth(CalendarMonth.MONTH_JULY).setSummaryDuration(90)))
                .build());

        TrainerWorkloadResponse result = client(Duration.ofSeconds(2)).findWorkload("Tra.Iner").orElseThrow();

        assertEquals("Tra.Iner", result.username());
        assertEquals("Tra", result.firstName());
        assertTrue(result.isActive());
        assertEquals(2026, result.years().get(0).year());
        assertEquals(Month.JULY, result.years().get(0).months().get(0).month());
        assertEquals(90, result.years().get(0).months().get(0).summaryDuration());
    }

    @Test
    public void findWorkload_sendsTheUsername() {
        fake.behaviour = (request, observer) -> reply(observer,
                trainer().setTrainerUsername(request.getTrainerUsername()).build());

        assertEquals("Some.One", client(Duration.ofSeconds(2)).findWorkload("Some.One").orElseThrow().username());
    }

    @Test
    public void findWorkload_notFound_returnsEmpty() {
        fake.behaviour = (request, observer) ->
                observer.onError(Status.NOT_FOUND.withDescription("no workload").asRuntimeException());

        assertEquals(Optional.empty(), client(Duration.ofSeconds(2)).findWorkload("Tra.Iner"));
    }

    @Test
    public void findWorkload_serverError_throwsUnavailable() {
        fake.behaviour = (request, observer) -> observer.onError(Status.INTERNAL.asRuntimeException());

        WorkloadUnavailableException ex = assertThrows(WorkloadUnavailableException.class,
                () -> client(Duration.ofSeconds(2)).findWorkload("Tra.Iner"));
        assertEquals(Status.Code.INTERNAL, ((StatusRuntimeException) ex.getCause()).getStatus().getCode());
    }

    @Test
    public void findWorkload_serverDown_throwsUnavailable() {
        server.shutdownNow();

        WorkloadUnavailableException ex = assertThrows(WorkloadUnavailableException.class,
                () -> client(Duration.ofSeconds(2)).findWorkload("Tra.Iner"));
        assertEquals(Status.Code.UNAVAILABLE, ((StatusRuntimeException) ex.getCause()).getStatus().getCode());
    }

    @Test
    public void findWorkload_slowServer_hitsDeadline() {
        fake.behaviour = (request, observer) -> { }; // never answers

        WorkloadUnavailableException ex = assertThrows(WorkloadUnavailableException.class,
                () -> client(Duration.ofMillis(100)).findWorkload("Tra.Iner"));
        assertEquals(Status.Code.DEADLINE_EXCEEDED, ((StatusRuntimeException) ex.getCause()).getStatus().getCode());
    }

    @Test
    public void findWorkload_deadlineIsPerCall_notPerClient() throws InterruptedException {
        fake.behaviour = (request, observer) -> reply(observer, trainer().build());
        GrpcWorkloadClient client = client(Duration.ofMillis(300));

        Thread.sleep(400); // longer than the deadline: a deadline fixed at creation would already be expired

        assertTrue(client.findWorkload("Tra.Iner").isPresent());
    }

    @Test
    public void findWorkload_skipsMonthsThisClientDoesNotKnow() {
        fake.behaviour = (request, observer) -> reply(observer, trainer()
                .addYears(YearlySummary.newBuilder().setYear(2026)
                        .addMonths(MonthlySummary.newBuilder().setMonthValue(0).setSummaryDuration(10))
                        .addMonths(MonthlySummary.newBuilder().setMonthValue(13).setSummaryDuration(20))
                        .addMonths(MonthlySummary.newBuilder().setMonth(CalendarMonth.MONTH_MAY).setSummaryDuration(30)))
                .build());

        TrainerWorkloadResponse result = client(Duration.ofSeconds(2)).findWorkload("Tra.Iner").orElseThrow();

        assertEquals(1, result.years().get(0).months().size());
        assertEquals(Month.MAY, result.years().get(0).months().get(0).month());
    }
}
