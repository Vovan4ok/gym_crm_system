package org.volodymyrzganiaiko.gym.crm.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.Training;
import org.volodymyrzganiaiko.gym.crm.system.service.TrainingService;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TrainingServiceImpl implements TrainingService {
    private TrainingDAO trainingDAO;
    private TrainerDAO trainerDAO;
    private TraineeDAO traineeDAO;
    private Validator validator;

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    @Autowired
    public void setTrainingDAO(TrainingDAO trainingDAO) {
        this.trainingDAO = trainingDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName, LocalDate trainingDate, Integer durationInMinutes) {
        Trainee trainee = traineeDAO.findByUsername(traineeUsername).orElseThrow(() -> new IllegalArgumentException("Trainee with username " + traineeUsername + " was not found"));
        Trainer trainer = trainerDAO.findByUsername(trainerUsername).orElseThrow(() -> new IllegalArgumentException("Trainer with username " + trainerUsername + " was not found"));
        Training training = new Training(trainee, trainer, trainer.getSpecialization(), trainingName, trainingDate, durationInMinutes);
        Set<ConstraintViolation<Training>> violations = validator.validate(training);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        trainee.getTrainers().add(trainer);
        training = trainingDAO.save(training);
        log.info("Creating the training record with id {}", training.getId());
        return training;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> findById(Long id) {
        log.debug("Finding the training record with id {}", id);
        return trainingDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername, LocalDate from, LocalDate to, String trainerUsername, String trainingTypeName) {
        log.debug("Finding the training records for trainee with username {}", traineeUsername);
        return trainingDAO.findTraineeTrainings(traineeUsername, from, to, trainerUsername, trainingTypeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername, LocalDate from, LocalDate to, String traineeUsername) {
        log.debug("Finding the training records for trainer with username {}", trainerUsername);
        return trainingDAO.findTrainerTrainings(trainerUsername, from, to, traineeUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findAll() {
        log.debug("Finding all training records");
        return trainingDAO.findAll();
    }
}
