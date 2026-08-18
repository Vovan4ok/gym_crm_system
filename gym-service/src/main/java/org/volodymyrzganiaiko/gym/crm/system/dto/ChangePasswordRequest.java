package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "New password for the authenticated user")
public record ChangePasswordRequest(
        @Schema(description = "Password that replaces the current one. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String newPassword) {
}