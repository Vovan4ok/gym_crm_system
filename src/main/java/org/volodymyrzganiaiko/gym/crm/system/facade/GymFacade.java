package org.volodymyrzganiaiko.gym.crm.system.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.mapper.DtoMapper;
import org.volodymyrzganiaiko.gym.crm.system.metrics.GymMetrics;
import org.volodymyrzganiaiko.gym.crm.system.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;
    private final AuthenticationService authenticationService;
    private final CredentialsService credentialsService;
    private final UserService userService;
    private final DtoMapper mapper;
    private final GymMetrics gymMetrics;

    private static final int MAX_REGISTRATION_ATTEMPTS = 3;
    private static final Logger log =  LoggerFactory.getLogger(GymFacade.class);

    @Autowired
    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, TrainingTypeService trainingTypeService, AuthenticationService authenticationService, CredentialsService credentialsService, UserService userService, DtoMapper mapper, GymMetrics gymMetrics) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
        this.authenticationService = authenticationService;
        this.credentialsService = credentialsService;
        this.userService = userService;
        this.mapper = mapper;
        this.gymMetrics = gymMetrics;
    }

    public void login(Credentials credentials) {
        authenticationService.check(credentials);
    }

    @Transactional
    public void changeLogin(Credentials credentials, String newPassword) {
        authenticationService.check(credentials);
        userService.changePassword(credentials.username(), newPassword);
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse getTraineeProfile(Credentials credentials, String username) {
        authenticationService.check(credentials);
        Trainee trainee = traineeService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Trainee with the username " + username + " was not found"));
        return mapper.mapTraineeToTraineeProfileResponse(trainee);
    }

    public TraineeRegistrationDTO createTrainee(Trainee trainee) {
        return registerWithRetry("Trainee", () -> {
            String rawPassword = credentialsService.assignCredentials(trainee);
            Trainee saved = traineeService.create(trainee);
            return new TraineeRegistrationDTO(saved.getUsername(), rawPassword);
        });
    }

    @Transactional
    public TraineeProfileResponse updateTraineeProfile(Credentials credentials, String username, Trainee data) {
        authenticationService.check(credentials);
        Trainee trainee = traineeService.update(username, data.getFirstName(), data.getLastName(), data.getIsActive(), data.getDateOfBirth(), data.getAddress());
        return mapper.mapTraineeToTraineeProfileResponse(trainee);
    }

    @Transactional
    public void deleteTraineeProfile(Credentials credentials, String username) {
        authenticationService.check(credentials);
        traineeService.deleteByUsername(username);
    }

    @Transactional
    public void changeTraineeStatus(Credentials credentials, String username, Boolean isActive) {
        authenticationService.check(credentials);
        if (isActive) traineeService.activate(username);
        else traineeService.deactivate(username);
    }

    @Transactional
    public List<TraineeTrainingResponse> getTraineeTrainings(Credentials credentials, String username, LocalDate from, LocalDate to, String trainerName, String trainingType) {
        authenticationService.check(credentials);
        return trainingService.getTraineeTrainings(username, from, to, trainerName, trainingType).stream().map(mapper::mapTrainingToTraineeTrainingResponse).toList();
    }

    @Transactional
    public TrainerProfileResponse getTrainerProfile(Credentials credentials, String username) {
        authenticationService.check(credentials);
        Trainer trainer = trainerService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Trainer with the username " + username + " was not found"));
        return mapper.mapTrainerToTrainerProfileResponse(trainer);
    }

    @Transactional
    public TrainerProfileResponse updateTrainerProfile(Credentials credentials, String username, Trainer data) {
        authenticationService.check(credentials);
        Trainer trainer = trainerService.update(username, data.getFirstName(), data.getLastName(), data.getIsActive());
        return mapper.mapTrainerToTrainerProfileResponse(trainer);
    }

    @Transactional
    public void changeTrainerStatus(Credentials credentials, String username, Boolean isActive) {
        authenticationService.check(credentials);
        if (isActive) trainerService.activate(username);
        else trainerService.deactivate(username);
    }

    @Transactional
    public List<TrainerSummaryResponse> getUnassignedTrainers(Credentials credentials, String username) {
        authenticationService.check(credentials);
        return trainerService.getUnassignedTrainers(username).stream().map(mapper::mapTrainerToTrainerSummaryResponse).toList();
    }

    @Transactional
    public List<TrainerSummaryResponse> updateTrainers(Credentials credentials, String username, List<String> trainerUsernames) {
        authenticationService.check(credentials);
        return traineeService.updateTrainerList(username, trainerUsernames).stream().map(mapper::mapTrainerToTrainerSummaryResponse).toList();
    }

    @Transactional
    public List<TrainerTrainingResponse> getTrainerTrainings(Credentials credentials, String username, LocalDate from, LocalDate to, String traineeName) {
        authenticationService.check(credentials);
        return trainingService.getTrainerTrainings(username, from, to, traineeName).stream().map(mapper::mapTrainingToTrainerTrainingResponse).toList();
    }

    @Transactional
    public void createTraining(Credentials credentials, AddTrainingRequest req) {
        authenticationService.check(credentials);
        gymMetrics.timeTrainingCreation(() -> trainingService.addTraining(req.traineeUsername(), req.trainerUsername(), req.trainingName(), req.trainingDate(), req.trainingDuration()));
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes(Credentials credentials) {
        authenticationService.check(credentials);
        return trainingTypeService.findAll().stream().map(mapper::mapTrainingTypeToTrainingTypeResponse).toList();
    }

    public TrainerRegistrationDTO createTrainer(Trainer trainer) {
        return registerWithRetry("Trainer", () -> {
            String rawPassword = credentialsService.assignCredentials(trainer);
            Trainer saved = trainerService.create(trainer);
            return new TrainerRegistrationDTO(saved.getUsername(), rawPassword);
        });
    }

    private <R> R registerWithRetry(String role, Supplier<R> registration) {
        for (int i = 1; i <= MAX_REGISTRATION_ATTEMPTS; i++) {
            try {
                R result = registration.get();
                gymMetrics.recordRegistration(role.toLowerCase());
                return result;
            } catch (DataIntegrityViolationException e) {
                log.warn("{} attempt of {} registration was unsuccessful", i, role.toLowerCase());
                gymMetrics.recordUsernameCollision(role.toLowerCase());
                if (i == MAX_REGISTRATION_ATTEMPTS) {
                    throw new IllegalStateException(role + " creation failed after " + MAX_REGISTRATION_ATTEMPTS + " attempts", e);
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }
}
