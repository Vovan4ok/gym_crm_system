package org.volodymyrzganiaiko.gym.crm.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.TrainingTypeService;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private TrainingTypeDAO trainingTypeDAO;
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

    @Autowired
    public void setTrainingTypeDAO(TrainingTypeDAO trainingTypeDAO) {
        this.trainingTypeDAO = trainingTypeDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> findById(Long id) {
        log.debug("Finding the training type record with id {}", id);
        return trainingTypeDAO.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> findAll() {
        log.debug("Finding the training type records");
        return trainingTypeDAO.findAll();
    }
}
