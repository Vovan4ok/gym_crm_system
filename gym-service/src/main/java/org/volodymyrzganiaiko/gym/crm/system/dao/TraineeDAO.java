package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDAO {
    Trainee save(Trainee trainee);
    Trainee update(Trainee trainee);
    boolean deleteByUsername(String username);
    Optional<Trainee> findById(Long traineeId);
    Optional<Trainee> findByUsername(String username);
    List<Trainee> findAll();
}
