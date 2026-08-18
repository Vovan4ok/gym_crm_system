package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingTypeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDAOImpl implements TrainingTypeDAO {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Optional<TrainingType> findById(Long id) {
        Session session = entityManager.unwrap(Session.class);
        return Optional.ofNullable(session.get(TrainingType.class, id));
    }

    @Override
    public List<TrainingType> findAll() {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from TrainingType", TrainingType.class).list();
    }
}
