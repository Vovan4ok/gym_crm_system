package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.*;
import org.volodymyrzganiaiko.gym.crm.system.service.impl.TrainingServiceImpl;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {
    @Mock
    private TrainingDAO trainingDAO;

    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Training training;

    private Trainee trainee;

    private Trainer trainer;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        trainingService.setValidator(validator);
        trainee = new Trainee(LocalDate.parse("2003-08-11"), "Test address", "John", "Doe", "John.Doe", "random", true, new HashSet<>());
        trainee.setId(1L);
        trainer = new Trainer(new TrainingType(1L, "Yoga"), "Test", "Test", "Test.Test", "random", true, new HashSet<>());
        trainer.setId(2L);
        training = new Training();
        training.setId(1L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingDate(LocalDate.now());
        training.setTrainingType(new TrainingType(1L, "Yoga"));
        training.setTrainingName("Yoga");
        training.setTrainingDurationInMinutes(90);
    }

    @Test
    public void findById_success() {
        when(trainingDAO.findById(1L)).thenReturn(Optional.of((training)));

        Optional<Training> result = trainingService.findById(1L);

        assertTrue(result.isPresent());
        verify(trainingDAO).findById(1L);
    }

    @Test
    public void getTraineeTrainings() {
        when(trainingDAO.findTraineeTrainings(any(), any(), any(), any(), any())).thenReturn(List.of(training));

        List<Training> result = trainingService.getTraineeTrainings("John.Doe", null, null, null, null);

        assertFalse(result.isEmpty());
        verify(trainingDAO).findTraineeTrainings("John.Doe", null, null, null, null);
    }

    @Test
    public void getTrainerTrainings() {
        when(trainingDAO.findTrainerTrainings(any(), any(), any(), any())).thenReturn(List.of(training));

        List<Training> result = trainingService.getTrainerTrainings("John.Doe", null, null, null);

        assertFalse(result.isEmpty());
        verify(trainingDAO).findTrainerTrainings("John.Doe", null, null, null);
    }

    @Test
    public void findAll() {
        when(trainingDAO.findAll()).thenReturn(List.of(training));

        List<Training> result = trainingService.findAll();

        assertFalse(result.isEmpty());
        verify(trainingDAO).findAll();
    }

    @Test
    public void addTraining_success() {
        when(traineeDAO.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("Test.Test")).thenReturn(Optional.of(trainer));
        when(trainingDAO.save(any(Training.class))).thenReturn(training);

        trainingService.addTraining("John.Doe", "Test.Test", "Yoga", LocalDate.now(), 90);

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingDAO).save(captor.capture());

        assertSame(trainee, captor.getValue().getTrainee());
        assertSame(trainer, captor.getValue().getTrainer());
        assertEquals("Yoga", captor.getValue().getTrainingName());
        assertEquals(90, captor.getValue().getTrainingDurationInMinutes());
        assertSame(trainer.getSpecialization(), captor.getValue().getTrainingType());
        assertTrue(trainee.getTrainers().contains(trainer));
    }

    @Test
    public void addTraining_traineeNotFound() {
        when(traineeDAO.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 90));
        verifyNoInteractions(trainerDAO, trainingDAO);
    }

    @Test
    public void addTraining_trainerNotFound() {
        when(traineeDAO.findByUsername(any())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining("Tr.Ainee", "Tra.Iner", "Cardio", LocalDate.parse("2026-07-10"), 90));

        verifyNoInteractions(trainingDAO);
        assertTrue(trainee.getTrainers().isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTrainingArguments")
    public void addTraining_invalidData(String description, String trainingName, LocalDate trainingDate, Integer duration) {
        when(traineeDAO.findByUsername(any())).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername(any())).thenReturn(Optional.of(trainer));

        assertThrows(ConstraintViolationException.class, () -> trainingService.addTraining(trainee.getUsername(), trainer.getUsername(), trainingName, trainingDate, duration));
        verify(trainingDAO, never()).save(any());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    static Stream<Arguments> invalidTrainingArguments() {
        return Stream.of(
                Arguments.of("blank trainingName",  "", LocalDate.now(), 90),
                Arguments.of("null trainingDate", "Cardio", null, 90),
                Arguments.of("null duration", "Cardio", LocalDate.now(), null)
        );
    }
}
