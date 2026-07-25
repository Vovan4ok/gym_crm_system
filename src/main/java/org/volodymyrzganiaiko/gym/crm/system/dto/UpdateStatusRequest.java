package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "Requested activation state. Setting the state the profile already has is rejected.")
public record UpdateStatusRequest (
        @Schema(description = "True activates the profile, false deactivates it", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull Boolean isActive) {
}