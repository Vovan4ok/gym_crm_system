package org.volodymyrzganiaiko.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Newly issued token pair")
public record RefreshResponse(
        @Schema(description = "Signed JWT access token")
        String accessToken,
        @Schema(description = "New opaque refresh token; the previous one is no longer valid")
        String refreshToken) {
    @Override
    public String toString() {
        return "";
    }
}
