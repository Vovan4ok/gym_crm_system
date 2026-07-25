package org.volodymyrzganiaiko.gym.crm.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.service.TraineeService;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.volodymyrzganiaiko.gym.crm.system.utils.ValueValidator.requireNotBlank;

@Service
public class TraineeServiceImpl implements TraineeService {
    private TraineeDAO traineeDAO;
    private TrainerDAO trainerDAO;
    private PasswordEncoder passwordEncoder;
    private Validator validator;

    private static final Logger log =  LoggerFactory.getLogger(TraineeServiceImpl.class);

    @Autowired
    public void setTraineeDAO(TraineeDAO traineeDAO) {
        this.traineeDAO = traineeDAO;
    }

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    @Transactional
    public Trainee create(Trainee trainee) {
        Set<ConstraintViolation<Trainee>> violations = validator.validate(trainee);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        trainee = traineeDAO.save(trainee);
        log.info("Creating a trainee record with id {}", trainee.getId());
        return trainee;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findById(Long id) {
        log.debug("Finding the trainee record with id {}", id);
        return traineeDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding the trainee with username {}", username);
        return traineeDAO.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> findAll() {
        log.debug("Finding all trainee records");
        return traineeDAO.findAll();
    }

    @Override
    @Transactional
    public Trainee update(String username, String newFirstName, String newLastName, Boolean newIsActive, LocalDate newDateOfBirth, String newAddress) {
        Trainee foundTrainee = getByUsernameOrThrow(username);
        foundTrainee.setFirstName(newFirstName);
        foundTrainee.setLastName(newLastName);
        foundTrainee.setIsActive(newIsActive);
        foundTrainee.setDateOfBirth(newDateOfBirth);
        foundTrainee.setAddress(newAddress);
        Set<ConstraintViolation<Trainee>> violations = validator.validate(foundTrainee);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        log.info("Updating the trainee with username {}", username);
        return foundTrainee;
    }

    @Override
    @Transactional
    public void activate(String username) {
        Trainee foundTrainee = getByUsernameOrThrow(username);
        foundTrainee.changeStatus(true);
        log.info("Activating the trainee with username {}", username);
    }

    @Override
    @Transactional
    public void deactivate(String username) {
        Trainee foundTrainee = getByUsernameOrThrow(username);
        foundTrainee.changeStatus(false);
        log.info("Deactivating the trainee with username {}", username);
    }

    @Override
    @Transactional
    public boolean deleteByUsername(String username) {
        log.info("Deleting the trainee with username {}", username);
        return traineeDAO.deleteByUsername(username);
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainerList(String username, List<String> trainerUsernames) {
        Trainee trainee = getByUsernameOrThrow(username);
        Set<Trainer> updatedTrainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            Optional<Trainer> trainer = trainerDAO.findByUsername(trainerUsername);
            if (trainer.isEmpty()) {
                log.warn("Trainer with username {} was not found", trainerUsername);
                throw new IllegalArgumentException("Trainer with username " + trainerUsername + " was not found");
            }
            updatedTrainers.add(trainer.get());
        }
        trainee.setTrainers(updatedTrainers);
        log.info("Updating trainerList for the trainee with username {}", username);
        return updatedTrainers.stream().toList();
    }

    private Trainee getByUsernameOrThrow(String username) {
        Optional<Trainee> foundTraineeOpt = traineeDAO.findByUsername(username);
        if (foundTraineeOpt.isEmpty()) {
            log.warn("Trainee with username {} was not found", username);
            throw new IllegalArgumentException("Trainee with the username " + username + " was not found");
        }
        return foundTraineeOpt.get();
    }
}
