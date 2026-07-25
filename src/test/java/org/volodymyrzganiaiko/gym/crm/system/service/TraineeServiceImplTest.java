package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.service.impl.TraineeServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {
    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private TrainerDAO trainerDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private Trainee trainee;

    @BeforeEach
    public void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        traineeService.setValidator(validator);
        trainee = new Trainee(null, null, "John", "Doe", "John.Doe", "random", true, new HashSet<>());
        trainee.setId(1L);
    }

    @Test
    public void create_generatesCredentials_andSaves() {
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.create(trainee);

        assertTrue(result.getIsActive());
        assertEquals("John.Doe", result.getUsername());
        verify(traineeDAO).save(trainee);
    }

    @Test
    public void findByUsername_positiveCase() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.ofNullable(trainee));

        Optional<Trainee> result = traineeService.findByUsername(trainee.getUsername());

        assertTrue(result.isPresent());
    }

    @Test
    public void update_updatesTrainee_success() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        trainee.setDateOfBirth(LocalDate.parse("2003-08-11"));
        trainee.setAddress("Test address");
        trainee.setFirstName("Test");
        trainee.setLastName("Test");

        Trainee result = traineeService.update(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName(), trainee.getIsActive(), trainee.getDateOfBirth(), trainee.getAddress());

        assertTrue(result.getIsActive());
        assertEquals("Test", result.getFirstName());
        assertEquals("Test", result.getLastName());
        assertEquals("Test address", result.getAddress());
        assertEquals(LocalDate.parse("2003-08-11"), result.getDateOfBirth());
    }

    @Test
    public void update_blankFirstName_throwsException() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        trainee.setFirstName("");

        assertThrows(ConstraintViolationException.class, () -> traineeService.update(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName(), trainee.getIsActive(), trainee.getDateOfBirth(), trainee.getAddress()));
    }

    @Test
    public void update_traineeNotFound_throwsException() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.empty());

        trainee.setFirstName("Test");

        assertThrows(IllegalArgumentException.class, () -> traineeService.update(trainee.getUsername(), trainee.getFirstName(), trainee.getLastName(), trainee.getIsActive(), trainee.getDateOfBirth(), trainee.getAddress()));
    }

    @Test
    public void activateTrainee_success() {
        trainee.setIsActive(false);
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        traineeService.activate(trainee.getUsername());

        assertTrue(trainee.getIsActive());
    }

    @Test
    public void activateTrainee_throwsException() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.activate(trainee.getUsername()));
    }

    @Test
    public void deactivateTrainee_success() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        traineeService.deactivate(trainee.getUsername());

        assertFalse(trainee.getIsActive());
    }

    @Test
    public void deactivateTrainee_throwsException() {
        trainee.setIsActive(false);
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class, () -> traineeService.deactivate(trainee.getUsername()));
    }

    @Test
    public void deleteByUsername_success() {
        when(traineeDAO.deleteByUsername(any(String.class))).thenReturn(true);

        boolean result = traineeService.deleteByUsername(trainee.getUsername());

        assertTrue(result);
        verify(traineeDAO).deleteByUsername(trainee.getUsername());
    }

    @Test
    public void updateTrainersList_success() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("trainer1")).thenReturn(Optional.of(new Trainer()));

        traineeService.updateTrainerList(trainee.getUsername(), List.of("trainer1"));

        assertEquals(1, trainee.getTrainers().size());
    }

    @Test
    public void updateTrainersList_trainerNotFound() {
        when(traineeDAO.findByUsername(any(String.class))).thenReturn(Optional.of(trainee));
        when(trainerDAO.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainerList(trainee.getUsername(), List.of("ghost")));
    }

    @Test
    public void findById_traineeFound() {
        when(traineeDAO.findById(any(Long.class))).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.findById(trainee.getId());

        assertTrue(result.isPresent());
        verify(traineeDAO).findById(trainee.getId());
    }

    @Test
    public void findById_traineeNotFound() {
        when(traineeDAO.findById(any(Long.class))).thenReturn(Optional.empty());
        Long input = 2L;

        Optional<Trainee> result = traineeService.findById(input);

        assertFalse(result.isPresent());
        verify(traineeDAO).findById(input);
    }

    @Test
    public void findAll_success() {
        when(traineeDAO.findAll()).thenReturn(new ArrayList<>());

        List<Trainee> result = traineeService.findAll();

        assertTrue(result.isEmpty());
        verify(traineeDAO).findAll();
    }
}
