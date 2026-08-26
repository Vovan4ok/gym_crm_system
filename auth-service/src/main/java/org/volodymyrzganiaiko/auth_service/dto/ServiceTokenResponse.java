package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Issued service access token")
public record ServiceTokenResponse(
        @Schema(description = "Signed JWT access token for the service")
        String token,
        @Schema(description = "Token lifetime in seconds")
        long expiresInSeconds) {
    @Override
    public String toString() {
        return "";
    }
}
