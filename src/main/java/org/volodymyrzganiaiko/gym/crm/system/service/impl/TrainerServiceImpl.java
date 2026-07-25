package org.volodymyrzganiaiko.gym.crm.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.TrainerService;

import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.volodymyrzganiaiko.gym.crm.system.utils.ValueValidator.requireNotBlank;

@Service
public class TrainerServiceImpl implements TrainerService {
    private TrainerDAO trainerDAO;
    private TrainingTypeDAO trainingTypeDAO;
    private PasswordEncoder passwordEncoder;
    private Validator validator;

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    @Autowired
    public void setTrainerDAO(TrainerDAO trainerDAO) {
        this.trainerDAO = trainerDAO;
    }

    @Autowired
    public void setTrainingTypeDAO(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
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
    public Trainer create(Trainer trainer) {
        trainer.setSpecialization(resolveSpecialization(trainer.getSpecialization()));
        Set<ConstraintViolation<Trainer>> violations = validator.validate(trainer);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        trainer = trainerDAO.save(trainer);
        log.info("Creating the trainer record with id {}", trainer.getId());
        return trainer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findById(Long id) {
        log.debug("Finding the trainer record with id {}", id);
        return trainerDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding the trainer with username {}", username);
        return trainerDAO.findByUsername(username);
    }

    @Override
    @Transactional
    public Trainer update(String username, String newFirstName, String newLastName, Boolean isActive) {
        Trainer foundTrainer = getByUsernameOrThrow(username);
        foundTrainer.setFirstName(newFirstName);
        foundTrainer.setLastName(newLastName);
        foundTrainer.setIsActive(isActive);
        Set<ConstraintViolation<Trainer>> violations = validator.validate(foundTrainer);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        log.info("Updating the trainer with username {}", username);
        return foundTrainer;
    }

    @Override
    @Transactional
    public void activate(String username) {
        Trainer foundTrainer = getByUsernameOrThrow(username);
        foundTrainer.changeStatus(true);
        log.info("Activating the trainer with username {}", username);
    }

    @Override
    @Transactional
    public void deactivate(String username) {
        Trainer foundTrainer = getByUsernameOrThrow(username);
        foundTrainer.changeStatus(false);
        log.info("Deactivating the trainer with username {}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String username) {
        log.debug("Finding unassigned trainers for the trainee with username {}", username);
        return trainerDAO.findUnassignedTrainers(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAll() {
        log.debug("Finding the trainers records");
        return trainerDAO.findAll();
    }

    private TrainingType resolveSpecialization(TrainingType specialization) {
        if (specialization == null || specialization.getId() == null) {
            throw new IllegalArgumentException("Specialization (training type) id is required");
        }
        return trainingTypeDAO.findById(specialization.getId()).orElseThrow(() -> new IllegalArgumentException("Training type with id " + specialization.getId() + " not found"));
    }

    private Trainer getByUsernameOrThrow(String username) {
        Optional<Trainer> foundTrainerOpt = trainerDAO.findByUsername(username);
        if (foundTrainerOpt.isEmpty()) {
            log.warn("Trainer with username {} wasn't found", username);
            throw new IllegalArgumentException("Trainer with the username " + username + " was not found");
        }
        return foundTrainerOpt.get();
    }
}
