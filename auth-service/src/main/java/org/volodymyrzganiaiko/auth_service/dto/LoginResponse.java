package org.volodymyrzganiaiko.auth_service.dto;

public record LoginResponse(String token) {
    @Override
    public String toString() {
        return "";
    }
}
