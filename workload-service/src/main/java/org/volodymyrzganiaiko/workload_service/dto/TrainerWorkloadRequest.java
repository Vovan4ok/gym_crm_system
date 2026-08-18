package org.volodymyrzganiaiko.workload_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainerWorkloadRequest(@NotBlank String trainerUsername, @NotBlank String firstName, @NotBlank String lastName, @NotNull Boolean isActive, @NotNull LocalDate trainingDate, @NotNull @Positive Integer trainingDuration, @NotNull ActionType actionType) {
}
