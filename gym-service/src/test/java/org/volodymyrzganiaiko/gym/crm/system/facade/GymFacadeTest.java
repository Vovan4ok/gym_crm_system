package org.volodymyrzganiaiko.gym.crm.system.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.volodymyrzganiaiko.gym.crm.system.client.WorkloadClient;
import org.volodymyrzganiaiko.gym.crm.system.domain.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.event.TraineeDeletedWorkloadEvent;
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
    private CredentialsService credentialsService;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private WorkloadClient workloadClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

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
    public void changePassword_success() {
        gymFacade.changeLogin("John.Doe", "newPassword");

        verify(userService).changePassword("John.Doe", "newPassword");
    }

    @Test
    public void deleteTraineeProfile_noTrainings_stillDeletes() {
        String input = "Tr.Ainee";

        gymFacade.deleteTraineeProfile(input);

        verify(traineeService).deleteByUsername(input);
        verify(applicationEventPublisher).publishEvent(
                argThat((TraineeDeletedWorkloadEvent e) -> e.workloads().isEmpty()));
    }

    @Test
    public void deleteTraineeProfile_withTrainings_notifiesWorkloadPerTraining() {
        Trainer trainer1 = new Trainer();
        trainer1.setUsername("Tra.Iner");
        trainer1.setFirstName("Tra");
        trainer1.setLastName("Iner");
        trainer1.setIsActive(true);
        Trainer trainer2 = new Trainer();
        trainer2.setUsername("Tra.Iner.1");
        trainer2.setFirstName("Tra");
        trainer2.setLastName("Iner");
        trainer2.setIsActive(true);
        Trainee trainee = new Trainee();
        trainee.setUsername("Tra.Inee");
        when(trainingService.getTraineeTrainings("Tra.Inee", null, null, null, null)).thenReturn(List.of(
                new Training(1L, trainee, trainer1, new TrainingType(1L, "Yoga"), "morning yoga", LocalDate.parse("2024-01-10"), 60),
                new Training(2L, trainee, trainer2, new TrainingType(1L, "Yoga"), "morning yoga", LocalDate.parse("2024-01-10"), 60)
        ));

        gymFacade.deleteTraineeProfile("Tra.Inee");

        verify(traineeService).deleteByUsername("Tra.Inee");
        ArgumentCaptor<TraineeDeletedWorkloadEvent> captor =
                ArgumentCaptor.forClass(TraineeDeletedWorkloadEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        List<TrainerWorkloadRequest> sent = captor.getValue().workloads();
        assertEquals(2, sent.size());
        assertTrue(sent.stream().allMatch(r -> r.actionType() == ActionType.DELETE));
        assertTrue(sent.stream().anyMatch(r -> r.trainerUsername().equals("Tra.Iner")));
        assertTrue(sent.stream().anyMatch(r -> r.trainerUsername().equals("Tra.Iner.1")));
    }

    @Test
    public void changeTraineeStatus_activate() {
        String inputUsername = "Tr.Ainee";
        Boolean inputIsActive = true;

        gymFacade.changeTraineeStatus(inputUsername, inputIsActive);

        verify(traineeService).activate(inputUsername);
        verify(traineeService, never()).deactivate(any());
    }

    @Test
    public void changeTraineeStatus_deactivate() {
        String inputUsername = "Tr.Ainee";
        Boolean inputIsActive = false;

        gymFacade.changeTraineeStatus(inputUsername, inputIsActive);

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
        

        when(traineeService.findByUsername("Tr.Ainee")).thenReturn(Optional.of(trainee));
        when(dtoMapper.mapTraineeToTraineeProfileResponse(trainee)).thenReturn(expected);

        TraineeProfileResponse result = gymFacade.getTraineeProfile("Tr.Ainee");

        assertEquals(expected, result);
        
    }

    @Test
    public void getTraineeProfile_notFound() {
        when(traineeService.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gymFacade.getTraineeProfile("Tr.Ainee"));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    public void updateTraineeProfile_success() {
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

        TraineeProfileResponse result = gymFacade.updateTraineeProfile("Tr.Ainee", trainee);

        assertEquals(expected, result);
        
    }

    @Test
    public void getTraineeTrainings_success() {
        Training training = new Training();
        TraineeTrainingResponse expected = new TraineeTrainingResponse("Morning activity", LocalDate.parse("2003-08-11"), new TrainingTypeResponse(2L, "Cardio"), 60, "Tr.Ainee");
        when(trainingService.getTraineeTrainings("Tr.Ainee", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tra.Iner", "Cardio")).thenReturn(List.of(training));
        when(dtoMapper.mapTrainingToTraineeTrainingResponse(training)).thenReturn(expected);

        List<TraineeTrainingResponse> result = gymFacade.getTraineeTrainings("Tr.Ainee", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tra.Iner", "Cardio");

        assertEquals(List.of(expected), result);
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
        

        when(trainerService.findByUsername("Tra.Iner")).thenReturn(Optional.of(trainer));
        when(dtoMapper.mapTrainerToTrainerProfileResponse(trainer)).thenReturn(expected);

        TrainerProfileResponse result = gymFacade.getTrainerProfile("Tra.Iner");

        assertEquals(expected, result);
        
    }

    @Test
    public void getTrainerProfile_notFound() {
        when(trainerService.findByUsername(any())).thenReturn(Optional.empty());
        

        assertThrows(IllegalArgumentException.class, () -> gymFacade.getTrainerProfile("Tra.Iner"));
        verifyNoInteractions(dtoMapper);
    }

    @Test
    public void changeTrainerStatus_activate() {
        
        String inputUsername = "Tra.Iner";
        Boolean inputIsActive = true;

        gymFacade.changeTrainerStatus(inputUsername, inputIsActive);

        
        verify(trainerService).activate(inputUsername);
        verify(trainerService, never()).deactivate(any());
    }

    @Test
    public void changeTrainerStatus_deactivate() {
        
        String inputUsername = "Tra.Iner";
        Boolean inputIsActive = false;

        gymFacade.changeTrainerStatus(inputUsername, inputIsActive);

        
        verify(trainerService).deactivate(inputUsername);
        verify(trainerService, never()).activate(any());
    }

    @Test
    public void updateTrainerProfile_success() {
        
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

        TrainerProfileResponse result = gymFacade.updateTrainerProfile("Tra.Iner", trainer);

        assertEquals(expected, result);
        
    }

    @Test
    public void getTrainerTrainings_success() {
        
        Training training = new Training();
        TrainerTrainingResponse expected = new TrainerTrainingResponse("Morning activity", LocalDate.parse("2003-08-11"), new TrainingTypeResponse(2L, "Cardio"), 60, "Tr.Ainee");
        when(trainingService.getTrainerTrainings("Tra.Iner", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tr.Ainee")).thenReturn(List.of(training));
        when(dtoMapper.mapTrainingToTrainerTrainingResponse(training)).thenReturn(expected);

        List<TrainerTrainingResponse> result = gymFacade.getTrainerTrainings("Tra.Iner", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-31"), "Tr.Ainee");

        assertEquals(List.of(expected), result);
        
    }

    @Test
    public void getUnassignedTrainers_success() {
        
        Trainer trainer = new Trainer();
        TrainerSummaryResponse expected = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));
        when(trainerService.getUnassignedTrainers("Tr.Ainee")).thenReturn(List.of(trainer));
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(trainer)).thenReturn(expected);

        List<TrainerSummaryResponse> result = gymFacade.getUnassignedTrainers("Tr.Ainee");

        assertEquals(List.of(expected), result);
        
    }

    @Test
    public void updateTrainers_success() {
        
        List<String> trainerUsernames = List.of("Tra.Iner", "Other.Trainer");
        Trainer first = new Trainer();
        Trainer second = new Trainer();
        TrainerSummaryResponse expectedFirst = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));
        TrainerSummaryResponse expectedSecond = new TrainerSummaryResponse("Tra.Iner1", "Tra", "Iner", new TrainingTypeResponse(2L, "Cardio"));

        when(traineeService.updateTrainerList("Tr.Ainee", trainerUsernames)).thenReturn(List.of(first, second));
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(first)).thenReturn(expectedFirst);
        when(dtoMapper.mapTrainerToTrainerSummaryResponse(second)).thenReturn(expectedSecond);

        List<TrainerSummaryResponse> result = gymFacade.updateTrainers("Tr.Ainee", trainerUsernames);

        assertEquals(List.of(expectedFirst, expectedSecond), result);
        
    }

    @Test
    public void createTraining_success() {
        AddTrainingRequest req = new AddTrainingRequest("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 60);

        doAnswer(inv -> {
            inv.getArgument(0, Runnable.class).run();
            return null;
        }).when(gymMetrics).timeTrainingCreation(any());
        Trainer trainer = new Trainer();
        trainer.setUsername("Tra.Iner");
        trainer.setFirstName("Tra");
        trainer.setLastName("Iner");
        trainer.setIsActive(true);

        Training training = new Training(null, trainer, null, "Cardio", LocalDate.parse("2026-07-10"), 60);

        when(trainingService.addTraining("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 60))
                .thenReturn(training);

        gymFacade.createTraining(req);

        verify(workloadClient).sendWorkload(argThat(r ->
                r.actionType() == ActionType.ADD && r.trainerUsername().equals("Tra.Iner")), any());
        verify(trainingService).addTraining("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 60);
    }

    @Test
    public void getTrainingTypes_success() {
        
        TrainingType trainingType = new TrainingType(1L, "Cardio");
        TrainingTypeResponse expected = new TrainingTypeResponse(1L, "Cardio");

        when(trainingTypeService.findAll()).thenReturn(List.of(trainingType));
        when(dtoMapper.mapTrainingTypeToTrainingTypeResponse(trainingType)).thenReturn(expected);

        List<TrainingTypeResponse> result = gymFacade.getTrainingTypes();

        assertEquals(List.of(expected), result);
        
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

    @Test
    public void deleteTraining_success() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Tra.Iner");
        trainer.setFirstName("Tra");
        trainer.setLastName("Iner");
        trainer.setIsActive(true);
        trainer.setSpecialization(new TrainingType(1L, "Yoga"));
        Trainee trainee = new Trainee();
        trainee.setUsername("Tr.Ainee");
        trainee.setAddress("Test address");
        trainee.setIsActive(true);
        trainee.setDateOfBirth(LocalDate.parse("2003-11-08"));
        trainee.setFirstName("Tr");
        trainee.setLastName("Ainee");
        trainee.setTrainers(Set.of());
        when(trainingService.findById(1L)).thenReturn(Optional.of(new Training(1L, trainee, trainer, new TrainingType(1L, "Yoga"), "morning yoga", LocalDate.parse("2024-01-10"), 60)));

        gymFacade.deleteTraining(1L);

        verify(trainingService).deleteTraining(1L);
        verify(workloadClient).sendWorkload(argThat(r -> r.actionType()==ActionType.DELETE && r.trainerUsername().equals("Tra.Iner")), any());
    }

    @Test
    public void deleteTraining_notFound() {
        when(trainingService.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> gymFacade.deleteTraining(1L));
        verifyNoInteractions(workloadClient);
    }
}