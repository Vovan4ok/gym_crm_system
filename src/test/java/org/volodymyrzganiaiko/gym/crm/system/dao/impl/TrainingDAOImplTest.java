package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DaoTestConfig.class)
public class TrainingDAOImplTest {
    @Autowired
    private TrainingDAO trainingDAO;

    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private TrainerDAO trainerDAO;

    @PersistenceContext
    private EntityManager entityManager;

    private Trainee trainee;
    private Trainer yogaTrainer;

    @BeforeEach
    public void setUp() {
        trainee = traineeDAO.save(new Trainee(LocalDate.parse("2000-01-01"),
                "addr", "Ann", "Free", "Ann.Free", "p", true, new HashSet<>()));
        yogaTrainer = trainerDAO.save(new Trainer(new TrainingType(1L, "Yoga"),
                "Yo", "Ga", "Yo.Ga", "p", true, new HashSet<>()));
        Trainer cardioTrainer = trainerDAO.save(new Trainer(new TrainingType(2L, "Cardio"),
                "Car", "Dio", "Car.Dio", "p", true, new HashSet<>()));
        Trainee bob = traineeDAO.save(new Trainee(LocalDate.parse("1999-05-05"),
                "addr2", "Bob", "Busy", "Bob.Busy", "p", true, new HashSet<>()));
        flushAndClear();

        trainingDAO.save(new Training(null, trainee, yogaTrainer, new TrainingType(1L, "Yoga"),
                "morning yoga", LocalDate.parse("2024-01-10"), 60));
        trainingDAO.save(new Training(null, trainee, cardioTrainer, new TrainingType(2L, "Cardio"),
                "hard cardio", LocalDate.parse("2024-06-10"), 45));
        trainingDAO.save(new Training(null, bob, yogaTrainer, new TrainingType(1L, "Yoga"),
                "evening yoga", LocalDate.parse("2024-02-15"), 60));
        flushAndClear();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void save_and_findById() {
        Training training = trainingDAO.save(new Training(null, trainee, yogaTrainer, new TrainingType(1L, "Yoga"), "morning yoga", LocalDate.parse("2024-01-10"), 60));
        flushAndClear();

        Training reloaded = trainingDAO.findById(training.getId()).orElseThrow();
        assertNotNull(reloaded);
        assertEquals(training.getTrainingName(), reloaded.getTrainingName());
    }

    static Stream<Arguments> traineeFilterCases() {
        return Stream.of(
                Arguments.of(null, null, null, null, Set.of("morning yoga", "hard cardio")),
                Arguments.of(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-03-01"), null, null, Set.of("morning yoga")),
                Arguments.of(null, null, "Yo.Ga", null, Set.of("morning yoga")),
                Arguments.of(null, null, null, "Cardio", Set.of("hard cardio"))
        );
    }

    @ParameterizedTest
    @MethodSource("traineeFilterCases")
    public void findTraineeTrainings_appliesFilters(LocalDate from, LocalDate to,
                                                    String trainerUsername, String typeName, Set<String> expectedNames) {
        List<Training> result = trainingDAO.findTraineeTrainings(
                "Ann.Free", from, to, trainerUsername, typeName);

        Set<String> actualNames = result.stream()
                .map(Training::getTrainingName)
                .collect(Collectors.toSet());

        assertEquals(expectedNames, actualNames);
    }

    static Stream<Arguments> trainerFilterCases() {
        return Stream.of(
                Arguments.of(null, null, null,       Set.of("morning yoga", "evening yoga")),
                Arguments.of(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"), null, Set.of("morning yoga")),
                Arguments.of(null, null, "Bob.Busy", Set.of("evening yoga")),
                Arguments.of(null, null, "Ann.Free", Set.of("morning yoga"))
        );
    }

    @ParameterizedTest
    @MethodSource("trainerFilterCases")
    public void findTrainerTrainings_appliesFilters(LocalDate from, LocalDate to,
                                                    String traineeUsername, Set<String> expectedNames) {
        List<Training> result = trainingDAO.findTrainerTrainings(
                "Yo.Ga", from, to, traineeUsername);

        Set<String> actualNames = result.stream()
                .map(Training::getTrainingName)
                .collect(Collectors.toSet());

        assertEquals(expectedNames, actualNames);
    }
}
