package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Training session as seen from the trainer side")
public record TrainerTrainingResponse(
        @Schema(description = "Name of the training session", example = "Morning session")
        String trainingName,

        @Schema(description = "Date the session took place on", example = "2026-07-20")
        LocalDate trainingDate,

        @Schema(description = "Training type of the session")
        TrainingTypeResponse trainingType,

        @Schema(description = "Duration of the session in minutes", example = "60")
        Integer trainingDuration,

        @Schema(description = "Username of the trainee who attended the session", example = "Tr.Ainee")
        String traineeName) {
}