package org.volodymyrzganiaiko.workload_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A trainer's accumulated workload summary")
public record TrainerSummaryResponse(
        @Schema(description = "Trainer's username")
        String username,
        @Schema(description = "Trainer's first name")
        String firstName,
        @Schema(description = "Trainer's last name")
        String lastName,
        @Schema(description = "Whether the trainer is currently active")
        boolean active,
        @Schema(description = "Per-year workload breakdown")
        List<YearlySummaryResponse> years) {
}
