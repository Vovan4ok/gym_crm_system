package org.volodymyrzganiaiko.workload_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "A single training-workload change for a trainer")
public record TrainerWorkloadRequest(
        @Schema(description = "Username of the trainer whose workload changes. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String trainerUsername,
        @Schema(description = "Trainer's first name. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String firstName,
        @Schema(description = "Trainer's last name. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String lastName,
        @Schema(description = "Whether the trainer is currently active.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Boolean isActive,
        @Schema(description = "Date of the training being added or removed.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull LocalDate trainingDate,
        @Schema(description = "Training duration in minutes. Must be positive.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Integer trainingDuration,
        @Schema(description = "Whether to add or subtract the duration.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull ActionType actionType) {
}
