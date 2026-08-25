package org.volodymyrzganiaiko.gym.crm.system.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

public record VerifyCredentialsRequest(@NotBlank String username, @NotBlank String password) {
    @Override
    public @NonNull String toString() {
        return "";
    }
}
