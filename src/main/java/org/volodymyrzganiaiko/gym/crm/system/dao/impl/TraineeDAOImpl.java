package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.TraineeDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDAOImpl implements TraineeDAO {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Trainee save(Trainee trainee) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(trainee);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Session session = entityManager.unwrap(Session.class);
        return (Trainee) session.merge(trainee);
    }

    @Override
    public boolean deleteByUsername(String username) {
        Optional<Trainee> traineeOpt = findByUsername(username);
        if (traineeOpt.isEmpty()) {
            return false;
        }
        Trainee trainee = traineeOpt.get();
        Session session = entityManager.unwrap(Session.class);
        session.createQuery("delete from Training t where t.trainee.id = :id").setParameter("id", trainee.getId()).executeUpdate();
        session.remove(trainee);
        return true;
    }

    @Override
    public Optional<Trainee> findById(Long traineeId) {
        Session session = entityManager.unwrap(Session.class);
        return Optional.ofNullable(session.get(Trainee.class, traineeId));
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Trainee t where t.username = :username", Trainee.class).setParameter("username", username).uniqueResultOptional();
    }

    @Override
    public List<Trainee> findAll() {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Trainee", Trainee.class).list();
    }
}
