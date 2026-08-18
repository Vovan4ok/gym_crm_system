package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Full trainee profile, including the trainers currently assigned to them")
public record TraineeProfileResponse(
        @Schema(description = "Generated username of the trainee", example = "Tr.Ainee")
        String username,

        @Schema(description = "First name of the trainee", example = "John")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth, null when never provided", example = "1990-05-15")
        LocalDate dateOfBirth,

        @Schema(description = "Postal address, null when never provided", example = "Main st. 1")
        String address,

        @Schema(description = "Whether the profile is active", example = "true")
        Boolean isActive,

        @Schema(description = "Trainers assigned to this trainee, empty when none are assigned")
        List<TrainerSummaryResponse> trainers) {
}