package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Client-credentials for a trusted service")
public record ServiceTokenRequest(
        @Schema(description = "Client identifier of the calling service. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String clientId,
        @Schema(description = "Client secret of the calling service. Must not be blank.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String clientSecret) {
    @Override
    public String toString() {
        return "";
    }
}
