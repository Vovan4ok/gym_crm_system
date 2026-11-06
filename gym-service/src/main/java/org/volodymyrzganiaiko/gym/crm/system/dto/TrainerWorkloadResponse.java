package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Month;
import java.util.List;

@Schema(description = "A trainer's accumulated training workload, grouped by year and month")
public record TrainerWorkloadResponse(
        @Schema(description = "Generated username of the trainer", example = "Tra.Iner")
        String username,

        @Schema(description = "First name of the trainer", example = "Jane")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Roe")
        String lastName,

        @Schema(description = "Whether the trainer is currently active")
        Boolean isActive,

        @Schema(description = "Per-year workload; empty when the trainer has no trainings yet")
        List<Year> years) {

    @Schema(description = "Workload for a single year")
    public record Year(
            @Schema(description = "Calendar year", example = "2026")
            int year,

            @Schema(description = "Per-month workload")
            List<MonthTotal> months) {
    }

    @Schema(description = "Workload for a single month")
    public record MonthTotal(
            @Schema(description = "Calendar month", example = "JULY")
            Month month,

            @Schema(description = "Total training duration in minutes", example = "90")
            int summaryDuration) {
    }
}
