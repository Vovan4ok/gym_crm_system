package org.volodymyrzganiaiko.workload_service.rpc;

import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.workload_service.dto.MonthlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.YearlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.grpc.CalendarMonth;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;
import org.volodymyrzganiaiko.workload_service.grpc.MonthlySummary;
import org.volodymyrzganiaiko.workload_service.grpc.YearlySummary;

/**
 * Converts the service-layer summary (the same DTO the REST API returns) into
 * the protobuf messages of the gRPC contract.
 */
@Component
public class WorkloadProtoMapper {

    public GetWorkloadResponse toProto(TrainerSummaryResponse summary) {
        GetWorkloadResponse.Builder builder = GetWorkloadResponse.newBuilder()
                .setTrainerUsername(summary.username())
                .setActive(summary.active());
        // Protobuf setters reject null; proto3 has no null, an unset string is "".
        if (summary.firstName() != null) {
            builder.setFirstName(summary.firstName());
        }
        if (summary.lastName() != null) {
            builder.setLastName(summary.lastName());
        }
        summary.years().forEach(year -> builder.addYears(toProto(year)));
        return builder.build();
    }

    private YearlySummary toProto(YearlySummaryResponse year) {
        YearlySummary.Builder builder = YearlySummary.newBuilder().setYear(year.year());
        year.months().forEach(month -> builder.addMonths(toProto(month)));
        return builder.build();
    }

    private MonthlySummary toProto(MonthlySummaryResponse month) {
        return MonthlySummary.newBuilder()
                .setMonth(toProto(month.month()))
                .setSummaryDuration(month.summaryDuration())
                .build();
    }

    // CalendarMonth numbers 1..12 match java.time.Month#getValue(), so the
    // conversion is a lookup by number; 0 (UNSPECIFIED) is never produced.
    private CalendarMonth toProto(java.time.Month month) {
        return CalendarMonth.forNumber(month.getValue());
    }
}
