package org.volodymyrzganiaiko.gym.crm.system.dto;

import org.jspecify.annotations.NonNull;

public record ServiceTokenRequest(String clientId, String clientSecret) {
    @Override
    public @NonNull String toString() {
        return "";
    }
}
