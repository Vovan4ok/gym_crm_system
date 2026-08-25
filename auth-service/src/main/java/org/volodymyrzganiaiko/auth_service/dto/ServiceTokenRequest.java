package org.volodymyrzganiaiko.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ServiceTokenRequest(@NotBlank String clientId, @NotBlank String clientSecret) {
    @Override
    public String toString() {
        return "";
    }
}
