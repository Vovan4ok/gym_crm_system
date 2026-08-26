package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Issued token pair")
public record LoginResponse(
        @Schema(description = "Signed JWT access token")
        String accessToken,
        @Schema(description = "Opaque refresh token used to obtain a new access token")
        String refreshToken) {
    @Override
    public String toString() {
        return "";
    }
}
