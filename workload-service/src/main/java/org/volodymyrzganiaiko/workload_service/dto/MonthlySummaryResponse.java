package org.volodymyrzganiaiko.workload_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Month;

@Schema(description = "Workload for a single month")
public record MonthlySummaryResponse(
        @Schema(description = "Calendar month")
        Month month,
        @Schema(description = "Total training duration in minutes for the month")
        int summaryDuration) {
}
