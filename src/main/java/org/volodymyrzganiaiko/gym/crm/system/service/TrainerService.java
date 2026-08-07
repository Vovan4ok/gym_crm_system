package org.volodymyrzganiaiko.gym.crm.system.service;

import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);
    Optional<Trainer> findById(Long id);
    Optional<Trainer> findByUsername(String username);
    Trainer update(String username, String newFirstName, String newLastName, Boolean isActive);
    void activate(String username);
    void deactivate(String username);
    List<Trainer> getUnassignedTrainers(String username);
    List<Trainer> findAll();
}
