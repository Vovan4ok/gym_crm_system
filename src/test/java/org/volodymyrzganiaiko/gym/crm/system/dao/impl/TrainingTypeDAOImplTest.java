package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(DaoTestConfig.class)
public class TrainingTypeDAOImplTest {
    @Autowired
    private TrainingTypeDAO trainingTypeDAO;

    @Test
    public void findAll_returns2Items() {
        List<TrainingType> result = trainingTypeDAO.findAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2,  result.size());
    }

    @Test
    public void findById_returnsTrainingType() {
        Optional<TrainingType> result = trainingTypeDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Yoga", result.get().getTrainingTypeName());
    }

    @Test
    public void findById_returnsEmpty() {
        Optional<TrainingType> result = trainingTypeDAO.findById(99L);

        assertTrue(result.isEmpty());
    }
}
