package org.volodymyrzganiaiko.workload_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Workload for a single year")
public record YearlySummaryResponse(
        @Schema(description = "Calendar year")
        int year,
        @Schema(description = "Per-month workload breakdown")
        List<MonthlySummaryResponse> months) {
}
