package org.volodymyrzganiaiko.gym.crm.system.dto;

import org.jspecify.annotations.NonNull;

public record ServiceTokenResponse(String token, long expiresInSeconds) {
    @Override
    public @NonNull String toString() {
        return "";
    }
}
