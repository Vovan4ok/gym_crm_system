package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.impl.TrainingTypeServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypeServiceImplTest {
    @Mock
    private TrainingTypeDAO trainingTypeDAO;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    public void findById_delegatesToDao() {
        TrainingType type = new TrainingType(1L, "Yoga");
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.of(type));

        Optional<TrainingType> result = trainingTypeService.findById(1L);

        assertTrue(result.isPresent());
        verify(trainingTypeDAO).findById(1L);
    }

    @Test
    public void findAll_delegatesToDao() {
        when(trainingTypeDAO.findAll()).thenReturn(List.of(new TrainingType(1L, "Yoga")));

        List<TrainingType> result = trainingTypeService.findAll();

        assertFalse(result.isEmpty());
        verify(trainingTypeDAO).findAll();
    }
}
