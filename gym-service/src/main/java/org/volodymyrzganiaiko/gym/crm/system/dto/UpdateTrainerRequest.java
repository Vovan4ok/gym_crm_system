package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "New state of a trainer profile. The specialization cannot be changed through this endpoint.")
public record UpdateTrainerRequest(
        @Schema(description = "First name of the trainer", requiredMode = Schema.RequiredMode.REQUIRED, example = "Jane")
        @NotBlank String firstName,

        @Schema(description = "Last name of the trainer", requiredMode = Schema.RequiredMode.REQUIRED, example = "Roe")
        @NotBlank String lastName,

        @Schema(description = "Activation state of the profile", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull Boolean isActive) {
}