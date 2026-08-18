package org.volodymyrzganiaiko.gym.crm.system.service;

import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);
    Optional<Trainee> findById(Long id);
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findAll();
    Trainee update(String username, String newFirstName, String newLastName, Boolean newIsActive, LocalDate newDateOfBirth, String newAddress);
    void activate(String username);
    void deactivate(String username);
    boolean deleteByUsername(String username);
    List<Trainer> updateTrainerList(String username, List<String> trainerUsernames);
}
