package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.impl.TrainerServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceImplTest {
    @Mock
    private TrainerDAO trainerDAO;

    @Mock
    private TrainingTypeDAO trainingTypeDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    Trainer trainer;

    @BeforeEach
    public void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        trainerService.setValidator(validator);
        trainer = new Trainer(new TrainingType(1L, "Yoga"), "John", "Doe", "John.Doe", "random", true, new HashSet<>());
        trainer.setId(1L);
    }

    @Test
    public void create_resolvesSpecialization_andSaves() {
        when(trainerDAO.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainingTypeDAO.findById(1L)).thenReturn(Optional.of(new TrainingType(1L, "Yoga")));

        Trainer result = trainerService.create(trainer);

        assertTrue(result.getIsActive());
        assertEquals("John.Doe", result.getUsername());
        verify(trainerDAO).save(trainer);
    }

    @Test
    public void create_throwsException() {
        trainer.setSpecialization(null);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
    }

    @Test
    public void findByUsername_positiveCase() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.ofNullable(trainer));

        Optional<Trainer> result = trainerService.findByUsername(trainer.getUsername());

        assertTrue(result.isPresent());
    }

    @Test
    public void update_updatesTrainer_success() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        trainer.setFirstName("Test");
        trainer.setLastName("Test");

        Trainer result = trainerService.update(trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(), trainer.getIsActive());

        assertTrue(result.getIsActive());
        assertEquals("Test", result.getFirstName());
        assertEquals("Test", result.getLastName());
    }

    @Test
    public void update_blankFirstName_throwsException() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        trainer.setFirstName("");

        assertThrows(ConstraintViolationException.class, () -> trainerService.update(trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(), trainer.getIsActive()));
    }

    @Test
    public void update_trainerNotFound_throwsException() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.empty());

        trainer.setFirstName("Test");

        assertThrows(IllegalArgumentException.class, () -> trainerService.update(trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(), trainer.getIsActive()));
    }

    @Test
    public void activateTrainer_success() {
        trainer.setIsActive(false);
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        trainerService.activate(trainer.getUsername());

        assertTrue(trainer.getIsActive());
    }

    @Test
    public void activateTrainer_throwsException() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.activate(trainer.getUsername()));
        verify(trainerDAO, never()).update(any());
    }

    @Test
    public void deactivateTrainer_success() {
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        trainerService.deactivate(trainer.getUsername());

        assertFalse(trainer.getIsActive());
    }

    @Test
    public void deactivateTrainer_throwsException() {
        trainer.setIsActive(false);
        when(trainerDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class, () -> trainerService.deactivate(trainer.getUsername()));
        verify(trainerDAO, never()).update(any());
    }

    @Test
    public void findById_trainerFound() {
        when(trainerDAO.findById(any(Long.class))).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.findById(trainer.getId());

        assertTrue(result.isPresent());
        verify(trainerDAO).findById(trainer.getId());
    }

    @Test
    public void findById_trainerNotFound() {
        when(trainerDAO.findById(any(Long.class))).thenReturn(Optional.empty());
        Long input = 2L;

        Optional<Trainer> result = trainerService.findById(input);

        assertFalse(result.isPresent());
        verify(trainerDAO).findById(input);
    }

    @Test
    public void findAll_success() {
        when(trainerDAO.findAll()).thenReturn(new ArrayList<>());

        List<Trainer> result = trainerService.findAll();

        assertTrue(result.isEmpty());
        verify(trainerDAO).findAll();
    }

    @Test
    public void getUnassignedTrainers() {
        when(trainerDAO.findUnassignedTrainers(any(String.class))).thenReturn(new ArrayList<>());

        List<Trainer> result = trainerService.getUnassignedTrainers("John.Doe");

        assertTrue(result.isEmpty());
        verify(trainerDAO).findUnassignedTrainers("John.Doe");
    }
}
