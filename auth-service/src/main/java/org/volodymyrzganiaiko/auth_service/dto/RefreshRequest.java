package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token to be exchanged for a new token pair")
public record RefreshRequest(
        @Schema(description = "Opaque refresh token issued at login or a previous refresh. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String refreshToken) {
    @Override
    public String toString() {
        return "";
    }
}
