package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a successful registration: generated credentials and an authentication token")
public record RegistrationResponse(
        @Schema(description = "Username generated from the first and last name, with a suffix when that combination is already taken", example = "John.Doe")
        String username,

        @Schema(description = "Generated password. Returned in this response only and stored encoded, so it cannot be retrieved again.")
        String password,

        @Schema(description = "JWT bearer token authenticating the newly created user. Pass it as 'Authorization: Bearer <token>' on subsequent calls.")
        String token) {
    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "username='" + username + '\'' +
                '}';
    }
}
