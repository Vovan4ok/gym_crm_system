package org.volodymyrzganiaiko.gym.crm.system.service;

import org.volodymyrzganiaiko.gym.crm.system.domain.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training addTraining(String traineeUsername, String trainerUsername, String trainingName, LocalDate trainingDate, Integer durationInMinutes);
    Optional<Training> findById(Long id);
    List<Training> getTraineeTrainings(String traineeUsername, LocalDate from, LocalDate to, String trainerUsername, String trainingTypeName);
    List<Training> getTrainerTrainings(String trainerUsername, LocalDate from, LocalDate to, String traineeUsername);
    List<Training> findAll();
}
