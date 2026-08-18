package org.volodymyrzganiaiko.gym.crm.system.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.Training;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DtoMapperTest {
    private final DtoMapper mapper = new DtoMapper();

    private TrainingType trainingType;
    private Trainer trainer;
    private Trainee trainee;

    @BeforeEach
    public void setUp() {
        trainingType = new TrainingType(2L, "Cardio");
        trainer = new Trainer(trainingType, "Jane", "Roe", "Jane.Roe", null, true, Set.of());
        trainee = new Trainee(LocalDate.parse("2003-11-08"), "Test address", "John", "Doe", "John.Doe", null, true, Set.of());
    }

    @Test
    public void mapTrainingTypeToTrainingTypeResponse_success() {
        TrainingTypeResponse result = mapper.mapTrainingTypeToTrainingTypeResponse(trainingType);

        assertEquals(2L, result.id());
        assertEquals("Cardio", result.name());
    }

    @Test
    public void mapTraineeToTraineeSummaryResponse_success() {
        TraineeSummaryResponse result = mapper.mapTraineeToTraineeSummaryResponse(trainee);

        assertEquals("John.Doe", result.username());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
    }

    @Test
    public void mapTrainerToTrainerSummaryResponse_success() {
        TrainerSummaryResponse result = mapper.mapTrainerToTrainerSummaryResponse(trainer);

        assertEquals("Jane.Roe", result.username());
        assertEquals("Jane", result.firstName());
        assertEquals("Roe", result.lastName());
        assertEquals(2L, result.specialization().id());
        assertEquals("Cardio", result.specialization().name());
    }

    @Test
    public void mapTraineeToTraineeProfileResponse_success() {
        trainee.setTrainers(Set.of(trainer));
        TraineeProfileResponse result = mapper.mapTraineeToTraineeProfileResponse(trainee);

        assertEquals("John.Doe", result.username());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(LocalDate.of(2003, 11, 8), result.dateOfBirth());
        assertTrue(result.isActive());
        assertEquals("Test address", result.address());
        assertEquals(1, result.trainers().size());
        assertEquals("Jane.Roe", result.trainers().get(0).username());
        assertEquals("Cardio", result.trainers().get(0).specialization().name());
    }

    @Test
    public void mapTrainerToTrainerProfileResponse_success() {
        trainer.setTrainees(Set.of(trainee));

        TrainerProfileResponse result = mapper.mapTrainerToTrainerProfileResponse(trainer);

        assertEquals("Jane.Roe", result.username());
        assertEquals("Jane", result.firstName());
        assertEquals("Roe", result.lastName());
        assertEquals("Cardio", result.specialization().name());
        assertEquals(true, result.isActive());
        assertEquals(1, result.trainees().size());
        assertEquals("John.Doe", result.trainees().get(0).username());
    }

    @Test
    public void mapTrainingToTraineeTrainingResponse_success() {
        Training training = getTraining();

        TraineeTrainingResponse result = mapper.mapTrainingToTraineeTrainingResponse(training);

        assertEquals("Morning session", result.trainingName());
        assertEquals(LocalDate.of(2026, 7, 20), result.trainingDate());
        assertEquals("Cardio", result.trainingType().name());
        assertEquals(60, result.trainingDuration());
        assertEquals("Jane.Roe", result.trainerName());
    }

    @Test
    public void mapTrainingToTrainerTrainingResponse_success() {
        Training training = getTraining();

        TrainerTrainingResponse result = mapper.mapTrainingToTrainerTrainingResponse(training);

        assertEquals("Morning session", result.trainingName());
        assertEquals(LocalDate.of(2026, 7, 20), result.trainingDate());
        assertEquals("Cardio", result.trainingType().name());
        assertEquals(60, result.trainingDuration());
        assertEquals("John.Doe", result.traineeName());
    }

    private Training getTraining() {
        Training training = new Training();
        training.setTrainingName("Morning session");
        training.setTrainingDate(LocalDate.of(2026, 7, 20));
        training.setTrainingType(trainingType);
        training.setTrainingDurationInMinutes(60);
        training.setTrainer(trainer);
        training.setTrainee(trainee);
        return training;
    }
}
