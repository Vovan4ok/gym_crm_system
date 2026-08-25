package org.volodymyrzganiaiko.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    @Override
    public String toString() {
        return "";
    }
}
