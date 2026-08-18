package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDAO {
    Trainer save(Trainer trainer);
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long trainerId);
    Optional<Trainer> findByUsername(String username);
    List<Trainer> findAll();
    List<Trainer> findUnassignedTrainers(String traineeUsername);
}
