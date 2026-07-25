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
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DaoTestConfig.class)
public class TraineeDAOImplTest {
    @Autowired
    private TraineeDAO traineeDAO;

    @PersistenceContext
    private EntityManager entityManager;

    private Trainee trainee;

    @BeforeEach
    public void setUpTrainee() {
        trainee = new Trainee(LocalDate.parse("2003-11-08"), "Test address", "John", "Doe", "John.Doe", "random", true, new HashSet<>());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void save_success() {
        Trainee result = traineeDAO.save(trainee);
        flushAndClear();

        Trainee reloaded = traineeDAO.findById(result.getId()).orElseThrow();
        assertNotNull(reloaded.getId());
        assertEquals("John.Doe", reloaded.getUsername());
        assertEquals("Test address", reloaded.getAddress());
    }

    @Test
    public void findByUsername_found() {
        Trainee savedTrainee = traineeDAO.save(trainee);
        flushAndClear();

        Optional<Trainee> foundTrainee = traineeDAO.findByUsername("John.Doe");
        assertTrue(foundTrainee.isPresent());
        assertEquals(foundTrainee.get().getId(), savedTrainee.getId());
    }

    @Test
    public void findByUsername_notFound() {
        Optional<Trainee> foundTrainee = traineeDAO.findByUsername("Test.Test");
        assertFalse(foundTrainee.isPresent());
    }

    @Test
    public void update_success() {
        trainee = traineeDAO.save(trainee);
        flushAndClear();

        trainee.setAddress("Test address modified");
        trainee = traineeDAO.update(trainee);
        flushAndClear();

        Trainee reloaded = traineeDAO.findById(trainee.getId()).orElseThrow();

        assertNotNull(reloaded);
        assertEquals("Test address modified", reloaded.getAddress());
    }

    @Test
    public void deleteByUsername_success() {
        trainee = traineeDAO.save(trainee);
        flushAndClear();

        boolean result = traineeDAO.deleteByUsername("John.Doe");
        assertTrue(result);

        Optional<Trainee> foundTraineeOpt = traineeDAO.findByUsername("John.Doe");
        assertTrue(foundTraineeOpt.isEmpty());

        Long count = entityManager.createQuery("select count(u) from User u where u.username = :username", Long.class).setParameter("username", "John.Doe").getSingleResult();
        assertEquals(0L, count);
    }
}
