package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "Training session to be recorded. The training type is taken from the trainer's specialization.")
public record AddTrainingRequest(
        @Schema(description = "Username of the trainee attending the session", requiredMode = Schema.RequiredMode.REQUIRED, example = "Tr.Ainee")
        @NotBlank String traineeUsername,

        @Schema(description = "Username of the trainer conducting the session", requiredMode = Schema.RequiredMode.REQUIRED, example = "Tra.Iner")
        @NotBlank String trainerUsername,

        @Schema(description = "Name of the training session", requiredMode = Schema.RequiredMode.REQUIRED, example = "Morning session")
        @NotBlank String trainingName,

        @Schema(description = "Date the session takes place on", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-07-20")
        @NotNull LocalDate trainingDate,

        @Schema(description = "Duration of the session in minutes. Must be positive.", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
        @NotNull @Positive Integer trainingDuration) {
}
