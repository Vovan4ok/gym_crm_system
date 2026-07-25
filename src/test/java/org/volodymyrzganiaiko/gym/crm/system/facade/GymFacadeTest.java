package org.volodymyrzganiaiko.gym.crm.system.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.volodymyrzganiaiko.gym.crm.system.domain.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.exception.AuthenticationException;
import org.volodymyrzganiaiko.gym.crm.system.mapper.DtoMapper;
import org.volodymyrzganiaiko.gym.crm.system.metrics.GymMetrics;
import org.volodymyrzganiaiko.gym.crm.system.service.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GymFacadeTest {
    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private TrainingTypeService trainingTypeService;
    
    @Mock
    private AuthenticationService authenticationService;
    
    @Mock
    private CredentialsService credentialsService;

    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    GymFacade gymFacade;

    @Test
    void createTrainee() {
        Trainee saved = new Trainee();
        saved.setUsername("John.Doe");
        when(credentialsService.assignCredentials(any())).thenReturn("test");
        when(traineeService.create(any())).thenReturn(saved);
        Trainee input = new Trainee();

        TraineeRegistrationDTO result = gymFacade.createTrainee(input);

        assertEquals("John.Doe", result.username());
        assertEquals("test", result.password());
        verify(credentialsService).assignCredentials(input);
        verify(traineeService).create(input);
    }

    @Test
    void createTrainer() {
        Trainer saved = new Trainer();
        saved.setUsername("John.Doe");
        when(credentialsService.assignCredentials(any())).thenReturn("test");
        when(trainerService.create(any())).thenReturn(saved);
        Trainer input = new Trainer();

        TrainerRegistrationDTO result = gymFacade.createTrainer(input);

        assertEquals("John.Doe", result.username());
        assertEquals("test", result.password());
        verify(credentialsService).assignCredentials(input);
        verify(trainerService).create(input);
    }

    @Test
    public void login_success() {
        Credentials credentials = new Credentials("John.Doe", "random");

        gymFacade.login(credentials);

        verify(authenticationService).check(credentials);
    }

    @Test
    public void login_invalidCredentials() {
        doThrow(new AuthenticationException("Wrong username or password")).when(authenticationService).check(any());
        Credentials credentials = new Credentials("John.Doe", "random");

        assertThrows(AuthenticationException.class, () -> gymFacade.login(credentials));
        verifyNoInteractions(traineeService, trainerService, trainingService, trainingTypeService);
    }

    @Test
    public void changePassword_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        gymFacade.changeLogin(credentials, "newPassword");

        verify(authenticationService).check(credentials);
        verify(userService).changePassword("John.Doe", "newPassword");
    }

    @Test
    public void changePassword_invalidCredentials() {
        doThrow(new AuthenticationException("Wrong password")).when(authenticationService).check(any());
        Credentials credentials = new Credentials("John.Doe", "random");

        assertThrows(AuthenticationException.class, () -> gymFacade.changeLogin(credentials, "newPassword"));
        verifyNoInteractions(userService);
    }

    @Test
    public void deleteTraineeProfile_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        String input = "Tr.Ainee";

        gymFacade.deleteTraineeProfile(credentials, input);

        verify(authenticationService).check(credentials);
        verify(traineeService).deleteByUsername(input);
    }

    @Test
    public void changeTraineeStatus_activate() {
        Credentials credentials = new Credentials("John.Doe", "random");
        String inputUsername = "Tr.Ainee";
        Boolean inputIsActive = true;

        gymFacade.changeTraineeStatus(credentials, inputUsername, inputIsActive);

        verify(authenticationService).check(credentials);
        verify(traineeService).activate(inputUsername);
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    public void changeTraineeStatus_deactivate() {
        Credentials credentials = new Credentials("John.Doe", "random");
        String inputUsername = "Tr.Ainee";
        Boolean inputIsActive = false;

        gymFacade.changeTraineeStatus(credentials, inputUsername, inputIsActive);

        verify(authenticationService).check(credentials);
        verify(traineeService).deactivate(inputUsername);
        verify(traineeService, never()).activate(any());
    }

    @Test
    public void getTraineeProfile_success() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Tr.Ainee");
        trainee.setAddress("Test address");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.parse("2003-11-08"));
        trainee.setFirstName("Tr");
        trainee.setLastName("Ainee");
        trainee.setTrainers(Set.of());
        TraineeProfileResponse expected = new TraineeProfileResponse("Tr.Ainee", "Tr", "Ainee", LocalDate.parse("2003-11-08"), "Test address", true, List.of());
        Credentials credentials = new Credentials("John.Doe", "random");

        when(traineeService.findByUsername("Tr.Ainee")).thenReturn(Optional.of(trainee));
        when(dtoMapper.mapTraineeToTraineeProfileResponse(trainee)).thenReturn(expected);

        TraineeProfileResponse result = gymFacade.getTraineeProfile(credentials, "Tr.Ainee");

        assertEquals(expected, result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getTraineeProfile_notFound() {
        when(traineeService.findByUsername(any())).thenReturn(Optional.empty());
        Credentials credentials = new Credentials("John.Doe", "random");

        assertThrows(IllegalArgumentException.class, () -> gymFacade.getTraineeProfile(credentials, "Tr.Ainee"));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    public void updateTraineeProfile_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        Trainee trainee = new Trainee();
        trainee.setFirstName("Jane");
        trainee.setLastName("Roe");
        trainee.setIsActive(false);
        trainee.setDateOfBirth(LocalDate.parse("2003-11-08"));
        trainee.setAddress("New st. 2");

        Trainee updated = new Trainee();
        updated.setFirstName("Jane");
        updated.setLastName("Roe");
        updated.setIsActive(false);

        TraineeProfileResponse expected = new TraineeProfileResponse("Tr.Ainee", "Jane", "Roe", LocalDate.parse("2003-11-08"), "New st. 2", false, List.of());

        when(traineeService.update("Tr.Ainee", "Jane", "Roe", false, LocalDate.parse("2003-11-08"), "New st. 2")).thenReturn(updated);
        when(dtoMapper.mapTraineeToTraineeProfileResponse(updated)).thenReturn(expected);

        TraineeProfileResponse result = gymFacade.updateTraineeProfile(credentials, "Tr.Ainee", trainee);

        assertEquals(expected, result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getTraineeTrainings_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        Training training = new Training();
        TraineeTrainingResponse expected = new TraineeTrainingResponse("Morning activity", LocalDate.parse("2003-08-11"), new TrainingTypeResponse(2L, "Cardio"), 60, "Tr.Ainee");
        when(trainingService.getTraineeTrainings("Tr.Ainee", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tra.Iner", "Cardio")).thenReturn(List.of(training));
        when(dtoMapper.mapTrainingToTraineeTrainingResponse(training)).thenReturn(expected);

        List<TraineeTrainingResponse> result = gymFacade.getTraineeTrainings(credentials, "Tr.Ainee", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tra.Iner", "Cardio");

        assertEquals(List.of(expected), result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getTrainerProfile_success() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Tra.Iner");
        trainer.setSpecialization(new TrainingType(1L, "Cardio"));
        trainer.setIsActive(true);
        trainer.setFirstName("Tr");
        trainer.setLastName("Ainer");
        trainer.setTrainees(Set.of());
        TrainerProfileResponse expected = new TrainerProfileResponse("Tra.Iner", "Tra", "Iner", new TrainingTypeResponse(1L, "Cardio"), true, List.of());
        Credentials credentials = new Credentials("John.Doe", "random");

        when(trainerService.findByUsername("Tra.Iner")).thenReturn(Optional.of(trainer));
        when(dtoMapper.mapTrainerToTrainerProfileResponse(trainer)).thenReturn(expected);

        TrainerProfileResponse result = gymFacade.getTrainerProfile(credentials, "Tra.Iner");

        assertEquals(expected, result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getTrainerProfile_notFound() {
        when(trainerService.findByUsername(any())).thenReturn(Optional.empty());
        Credentials credentials = new Credentials("John.Doe", "random");

        assertThrows(IllegalArgumentException.class, () -> gymFacade.getTrainerProfile(credentials, "Tra.Iner"));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    public void changeTrainerStatus_activate() {
        Credentials credentials = new Credentials("John.Doe", "random");
        String inputUsername = "Tra.Iner";
        Boolean inputIsActive = true;

        gymFacade.changeTrainerStatus(credentials, inputUsername, inputIsActive);

        verify(authenticationService).check(credentials);
        verify(trainerService).activate(inputUsername);
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    public void changeTrainerStatus_deactivate() {
        Credentials credentials = new Credentials("John.Doe", "random");
        String inputUsername = "Tra.Iner";
        Boolean inputIsActive = false;

        gymFacade.changeTrainerStatus(credentials, inputUsername, inputIsActive);

        verify(authenticationService).check(credentials);
        verify(trainerService).deactivate(inputUsername);
        verify(trainerService, never()).activate(any());
    }

    @Test
    public void updateTrainerProfile_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        Trainer trainer = new Trainer();
        trainer.setFirstName("Jane");
        trainer.setLastName("Roe");
        trainer.setIsActive(false);

        Trainer updated = new Trainer();
        updated.setFirstName("Jane");
        updated.setLastName("Roe");
        updated.setIsActive(false);

        TrainerProfileResponse expected = new TrainerProfileResponse("Tra.Iner", "Jane", "Roe", new TrainingTypeResponse(1L, "Cardio"), false, List.of());

        when(trainerService.update("Tra.Iner", "Jane", "Roe", false)).thenReturn(updated);
        when(dtoMapper.mapTrainerToTrainerProfileResponse(updated)).thenReturn(expected);

        TrainerProfileResponse result = gymFacade.updateTrainerProfile(credentials, "Tra.Iner", trainer);

        assertEquals(expected, result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getTrainerTrainings_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        Training training = new Training();
        TrainerTrainingResponse expected = new TrainerTrainingResponse("Morning activity", LocalDate.parse("2003-08-11"), new TrainingTypeResponse(2L, "Cardio"), 60, "Tr.Ainee");
        when(trainingService.getTrainerTrainings("Tra.Iner", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tr.Ainee")).thenReturn(List.of(training));
        when(dtoMapper.mapTrainingToTrainerTrainingResponse(training)).thenReturn(expected);

        List<TrainerTrainingResponse> result = gymFacade.getTrainerTrainings(credentials, "Tra.Iner", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tr.Ainee");

        assertEquals(List.of(expected), result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void getUnassignedTrainers_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        Trainer trainer = new Trainer();
        TrainerSummaryResponse expected = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));
        when(trainerService.getUnassignedTrainers("Tr.Ainee")).thenReturn(List.of(trainer));
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(trainer)).thenReturn(expected);

        List<TrainerSummaryResponse> result = gymFacade.getUnassignedTrainers(credentials, "Tr.Ainee");

        assertEquals(List.of(expected), result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void updateTrainers_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        List<String> trainerUsernames = List.of("Tra.Iner", "Other.Trainer");
        Trainer first = new Trainer();
        Trainer second = new Trainer();
        TrainerSummaryResponse expectedFirst = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));
        TrainerSummaryResponse expectedSecond = new TrainerSummaryResponse("Tra.Iner1", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));

        when(traineeService.updateTrainerList("Tr.Ainee", trainerUsernames)).thenReturn(List.of(first, second));
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(first)).thenReturn(expectedFirst);
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(second)).thenReturn(expectedSecond);

        List<TrainerSummaryResponse> result = gymFacade.updateTrainers(credentials, "Tr.Ainee", trainerUsernames);

        assertEquals(List.of(expectedFirst, expectedSecond), result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void createTraining_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        AddTrainingRequest req = new AddTrainingRequest("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 60);

        doAnswer(inv -> {
            inv.getArgument(0, Runnable.class).run();
            return null;
        }).when(gymMetrics).timeTrainingCreation(any());

        gymFacade.createTraining(credentials, req);

        verify(authenticationService).check(credentials);
        verify(trainingService).addTraining("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 60);
    }

    @Test
    public void getTrainingTypes_success() {
        Credentials credentials = new Credentials("John.Doe", "random");
        TrainingType trainingType = new TrainingType(1L, "Cardio");
        TrainingTypeResponse expected = new TrainingTypeResponse(1L, "Cardio");

        when(trainingTypeService.findAll()).thenReturn(List.of(trainingType));
        when(dtoMapper.mapTrainingTypeToTrainingTypeResponse(trainingType)).thenReturn(expected);

        List<TrainingTypeResponse> result = gymFacade.getTrainingTypes(credentials);

        assertEquals(List.of(expected), result);
        verify(authenticationService).check(credentials);
    }

    @Test
    public void createTrainee_retriesOnDuplicateUsername() {
        Trainee input = new Trainee();
        Trainee saved = new Trainee();
        saved.setUsername("John.Doe1");

        when(credentialsService.assignCredentials(any())).thenReturn("password");
        when(traineeService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate username"))
                .thenReturn(saved);

        TraineeRegistrationDTO result = gymFacade.createTrainee(input);

        verify(traineeService, times(2)).create(any());
        verify(credentialsService, times(2)).assignCredentials(any());
        assertEquals("John.Doe1", result.username());
        assertEquals("password", result.password());
    }

    @Test
    public void createTrainee_failsAfterMaxAttempts() {
        Trainee input = new Trainee();
        when(credentialsService.assignCredentials(any())).thenReturn("password");
        when(traineeService.create(any())).thenThrow(new DataIntegrityViolationException("duplicate username"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> gymFacade.createTrainee(input));

        verify(traineeService, times(3)).create(any());
        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Trainee"));
    }

    @Test
    public void createTrainer_retriesOnDuplicateUsername() {
        Trainer input = new Trainer();
        Trainer saved = new Trainer();
        saved.setUsername("John.Doe1");

        when(credentialsService.assignCredentials(any())).thenReturn("password");
        when(trainerService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate username"))
                .thenReturn(saved);

        TrainerRegistrationDTO result = gymFacade.createTrainer(input);

        verify(trainerService, times(2)).create(any());
        verify(credentialsService, times(2)).assignCredentials(any());
        assertEquals("John.Doe1", result.username());
        assertEquals("password", result.password());
    }

    @Test
    public void createTrainer_failsAfterMaxAttempts() {
        Trainer input = new Trainer();
        when(credentialsService.assignCredentials(any())).thenReturn("password");
        when(trainerService.create(any())).thenThrow(new DataIntegrityViolationException("duplicate username"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> gymFacade.createTrainer(input));

        verify(trainerService, times(3)).create(any());
        assertInstanceOf(DataIntegrityViolationException.class, ex.getCause());
        assertTrue(ex.getMessage().contains("Trainer"));
    }
}