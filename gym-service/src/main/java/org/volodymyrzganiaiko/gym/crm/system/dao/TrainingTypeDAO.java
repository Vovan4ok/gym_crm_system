package org.volodymyrzganiaiko.gym.crm.system.dao;

import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeDAO {
    Optional<TrainingType> findById(Long id);
    List<TrainingType> findAll();
}
