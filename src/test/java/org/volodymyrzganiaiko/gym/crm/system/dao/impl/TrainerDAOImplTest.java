package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DaoTestConfig.class)
public class TrainerDAOImplTest {
    @Autowired
    private TrainerDAO trainerDAO;

    @Autowired
    private TraineeDAO traineeDAO;

    @PersistenceContext
    EntityManager entityManager;

    Trainer trainer;

    @BeforeEach
    public void setUpTrainer() {

        trainer = new Trainer(new TrainingType(1L, "Yoga"), "John", "Doe", "John.Doe", "random", true, new HashSet<>());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void save_success() {
        trainer = trainerDAO.save(trainer);
        flushAndClear();

        Trainer reloaded = trainerDAO.findByUsername("John.Doe").orElseThrow();
        assertNotNull(reloaded.getId());
        assertEquals("John.Doe", reloaded.getUsername());
        assertEquals("Yoga", reloaded.getSpecialization().getTrainingTypeName());
    }

    @Test
    public void findById_found() {
        trainer = trainerDAO.save(trainer);
        flushAndClear();

        Optional<Trainer> reloaded =  trainerDAO.findById(trainer.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(trainer.getUsername(), reloaded.get().getUsername());
    }

    @Test
    public void findById_not_found() {
        trainer = trainerDAO.save(trainer);
        flushAndClear();

        Optional<Trainer> reloaded =  trainerDAO.findById(99L);
        assertTrue(reloaded.isEmpty());
    }

    @Test
    public void update_success() {
        trainer = trainerDAO.save(trainer);
        flushAndClear();

        trainer.setFirstName("Test");
        trainer = trainerDAO.update(trainer);
        flushAndClear();

        Trainer reloaded = trainerDAO.findById(trainer.getId()).orElseThrow();
        assertEquals("Test", reloaded.getFirstName());
    }

    @Test
    public void findByUsername_found() {
        Trainer savedTrainer = trainerDAO.save(trainer);
        flushAndClear();

        Optional<Trainer> foundTrainer = trainerDAO.findByUsername("John.Doe");
        assertTrue(foundTrainer.isPresent());
        assertEquals(foundTrainer.get().getId(), savedTrainer.getId());
    }

    @Test
    public void findByUsername_notFound() {
        Optional<Trainer> foundTrainer = trainerDAO.findByUsername("Test.Test");
        assertFalse(foundTrainer.isPresent());
    }

    @Test
    public void findUnassignedTrainers_Found() {
        Trainer trainer1 = new Trainer(new TrainingType(2L, "Cardio"), "Bob", "Busy", "Bob.Busy", "random", true, new HashSet<>());
        Trainee trainee = new Trainee(LocalDate.parse("2011-08-11"), "Test address", "Ann", "Free", "Ann.Free", "random", true, new HashSet<>());
        trainer = trainerDAO.save(trainer);
        trainerDAO.save(trainer1);
        trainee = traineeDAO.save(trainee);
        flushAndClear();

        trainee.setTrainers(Set.of(trainer));
        traineeDAO.update(trainee);
        flushAndClear();

        List<Trainer> result = trainerDAO.findUnassignedTrainers("Ann.Free");
        List<String> resultUsernames = result.stream().map(User::getUsername).toList();
        assertTrue(resultUsernames.contains("Bob.Busy"));
        assertFalse(resultUsernames.contains("John.Doe"));
    }

    @Test
    public void findUnassignedTrainers_NotFound() {
        Trainer trainer1 = new Trainer(new TrainingType(2L, "Cardio"), "Bob", "Busy", "Bob.Busy", "random", true, new HashSet<>());
        Trainee trainee = new Trainee(LocalDate.parse("2011-08-11"), "Test address", "Ann", "Free", "Ann.Free", "random", true, new HashSet<>());
        trainer = trainerDAO.save(trainer);
        trainer1 = trainerDAO.save(trainer1);
        trainee = traineeDAO.save(trainee);
        flushAndClear();

        trainee.setTrainers(Set.of(trainer, trainer1));
        traineeDAO.update(trainee);
        flushAndClear();

        List<Trainer> result = trainerDAO.findUnassignedTrainers("Ann.Free");
        List<String> resultUsernames = result.stream().map(User::getUsername).toList();
        assertTrue(resultUsernames.isEmpty());
    }

    @Test
    public void findAll() {
        trainer = trainerDAO.save(trainer);
        flushAndClear();

        List<Trainer> result = trainerDAO.findAll();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
