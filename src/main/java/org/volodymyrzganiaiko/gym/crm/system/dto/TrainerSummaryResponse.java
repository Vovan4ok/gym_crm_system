package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short trainer reference used inside a trainee profile and in trainer lists")
public record TrainerSummaryResponse(
        @Schema(description = "Generated username of the trainer", example = "Tra.Iner")
        String username,

        @Schema(description = "First name of the trainer", example = "Jane")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Roe")
        String lastName,

        @Schema(description = "Training type the trainer specializes in")
        TrainingTypeResponse specialization) {
}