package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "User credentials to authenticate with")
public record LoginRequest(
        @Schema(description = "Username of the trainee or trainer. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String username,
        @Schema(description = "Raw password. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String password) {
    @Override
    public String toString() {
        return "";
    }
}
