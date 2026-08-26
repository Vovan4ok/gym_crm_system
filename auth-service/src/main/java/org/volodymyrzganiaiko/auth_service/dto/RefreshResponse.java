package org.volodymyrzganiaiko.auth_service.dto;

public record RefreshResponse(String accessToken, String refreshToken) {
    @Override
    public String toString() {
        return "";
    }
}
