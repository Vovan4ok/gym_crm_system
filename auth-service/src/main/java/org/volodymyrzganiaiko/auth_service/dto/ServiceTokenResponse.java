package org.volodymyrzganiaiko.auth_service.dto;

public record ServiceTokenResponse(String token, long expiresInSeconds) {
    @Override
    public String toString() {
        return "";
    }
}
