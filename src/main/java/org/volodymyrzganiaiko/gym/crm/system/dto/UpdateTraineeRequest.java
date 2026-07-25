package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "New state of a trainee profile. Every field is overwritten, so omitting an optional field clears it.")
public record UpdateTraineeRequest (
        @Schema(description = "First name of the trainee", requiredMode = Schema.RequiredMode.REQUIRED, example = "John")
        @NotBlank String firstName,

        @Schema(description = "Last name of the trainee", requiredMode = Schema.RequiredMode.REQUIRED, example = "Doe")
        @NotBlank String lastName,

        @Schema(description = "Activation state of the profile", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull Boolean isActive,

        @Schema(description = "Date of birth, optional", example = "1990-05-15")
        LocalDate dateOfBirth,

        @Schema(description = "Postal address, optional", example = "Main st. 1")
        String address) {
}