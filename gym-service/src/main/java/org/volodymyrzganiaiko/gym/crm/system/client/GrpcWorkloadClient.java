package org.volodymyrzganiaiko.gym.crm.system.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.MonthlySummary;
import org.volodymyrzganiaiko.workload_service.grpc.WorkloadServiceGrpc;
import org.volodymyrzganiaiko.workload_service.grpc.YearlySummary;

import java.time.Duration;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcWorkloadClient implements WorkloadClient {
    private static final Logger log = LoggerFactory.getLogger(GrpcWorkloadClient.class);

    private final WorkloadServiceGrpc.WorkloadServiceBlockingStub stub;
    private final Duration deadline;

    public GrpcWorkloadClient(WorkloadServiceGrpc.WorkloadServiceBlockingStub stub,
                              @Value("${workload.grpc.deadline}") Duration deadline) {
        this.stub = stub;
        this.deadline = deadline;
    }

    @Override
    public Optional<TrainerWorkloadResponse> findWorkload(String trainerUsername) {
        GetWorkloadRequest request = GetWorkloadRequest.newBuilder()
                .setTrainerUsername(trainerUsername)
                .build();
        try {
            // A deadline is absolute, so it is set on every call, never once on the shared stub.
            GetWorkloadResponse response = stub
                    .withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .getWorkload(request);
            return Optional.of(toDto(response));
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.debug("workload-service has no workload for {}", trainerUsername);
                return Optional.empty();
            }
            log.warn("Workload query for {} failed: {}", trainerUsername, e.getStatus());
            throw new WorkloadUnavailableException("Workload service is temporarily unavailable", e);
        }
    }

    private TrainerWorkloadResponse toDto(GetWorkloadResponse response) {
        List<TrainerWorkloadResponse.Year> years = response.getYearsList().stream()
                .map(this::toDto)
                .toList();
        return new TrainerWorkloadResponse(response.getTrainerUsername(), response.getFirstName(),
                response.getLastName(), response.getActive(), years);
    }

    private TrainerWorkloadResponse.Year toDto(YearlySummary year) {
        List<TrainerWorkloadResponse.MonthTotal> months = year.getMonthsList().stream()
                .filter(this::hasKnownMonth)
                .map(m -> new TrainerWorkloadResponse.MonthTotal(Month.of(m.getMonthValue()), m.getSummaryDuration()))
                .toList();
        return new TrainerWorkloadResponse.Year(year.getYear(), months);
    }

    // getMonthValue() is the raw wire number: safe even for values this client's
    // contract version does not know (getMonth() would return UNRECOGNIZED).
    private boolean hasKnownMonth(MonthlySummary month) {
        int value = month.getMonthValue();
        if (value < 1 || value > 12) {
            log.warn("Skipping a workload entry with an unknown month value {}", value);
            return false;
        }
        return true;
    }
}
