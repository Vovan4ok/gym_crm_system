package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DaoTestConfig.class)
public class UserDAOImplTest {
    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private TrainerDAO trainerDAO;

    @Autowired
    private UserDAO userDAO;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void save2UsersWithSameUsernames_throwsException() {
        traineeDAO.save(new Trainee(LocalDate.parse("2003-11-08"), "Test address", "John", "Doe", "John.Doe", "random", true, new HashSet<>()));
        trainerDAO.save(new Trainer(new TrainingType(1L, "Yoga"), "John", "Doe", "John.Doe", "random", true, new HashSet<>()));


        assertThrows(Exception.class, this::flushAndClear);
    }

    @Test
    public void findByUsername_traineeFound() {
        traineeDAO.save(new Trainee(LocalDate.parse("2003-11-08"), "Test address", "John", "Doe", "John.Doe", "random", true, new HashSet<>()));
        flushAndClear();

        Optional<User> result = userDAO.findByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    public void findByUsername_trainerFound() {
        trainerDAO.save(new Trainer(new TrainingType(1L, "Yoga"), "John", "Doe", "John.Doe", "random", true, new HashSet<>()));
        flushAndClear();

        Optional<User> result = userDAO.findByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    public void findByUsername_userNotFound() {
        Optional<User> result = userDAO.findByUsername("John.Doe");

        assertFalse(result.isPresent());
    }

    @Test
    public void findAll() {
        traineeDAO.save(new Trainee(LocalDate.parse("2003-11-08"), "Test address", "John", "Doe", "John.Doe", "random", true, new HashSet<>()));
        trainerDAO.save(new Trainer(new TrainingType(1L, "Yoga"), "John", "Doe", "John.Doe.1", "random", true, new HashSet<>()));
        flushAndClear();

        List<User> result = userDAO.findAll();
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals("John.Doe", result.get(0).getUsername());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
