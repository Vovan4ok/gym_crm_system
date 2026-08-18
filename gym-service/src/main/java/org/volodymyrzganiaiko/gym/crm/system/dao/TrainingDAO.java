package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingDAO {
    Training save(Training training);
    Optional<Training> findById(Long trainingId);
    List<Training> findAll();
    List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerUsername, String trainingTypeName);
    List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeUsername);
}
