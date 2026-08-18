package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Full trainer profile, including the trainees currently assigned to them")
public record TrainerProfileResponse(
        @Schema(description = "Generated username of the trainer", example = "Tra.Iner")
        String username,

        @Schema(description = "First name of the trainer", example = "Jane")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Roe")
        String lastName,

        @Schema(description = "Training type the trainer specializes in")
        TrainingTypeResponse specialization,

        @Schema(description = "Whether the profile is active", example = "true")
        Boolean isActive,

        @Schema(description = "Trainees assigned to this trainer, empty when none are assigned")
        List<TraineeSummaryResponse> trainees) {
}