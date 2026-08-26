package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.AbstractPostgresIT;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DaoTestConfig.class)
public class UserOptimisticLockIT extends AbstractPostgresIT {
    @Autowired
    private TraineeDAO traineeDAO;

    @Autowired
    private UserDAO userDAO;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void staleUpdate_throwsOptimisticLock() {
        traineeDAO.save(new Trainee(LocalDate.parse("2003-11-08"), "Test", "Tra", "Inee", "Tra.Inee", "random", true, Set.of()));
        flushAndClear();

        User stale = userDAO.findByUsername("Tra.Inee").get();
        entityManager.detach(stale);

        User fresh = userDAO.findByUsername("Tra.Inee").get();
        fresh.setFirstName("Updated");
        flushAndClear();

        stale.setFirstName("Conflict");

        assertThrows(OptimisticLockException.class, () -> {
            entityManager.merge(stale);
            entityManager.flush();
        });
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}