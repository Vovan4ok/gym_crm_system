package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainerDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAOImpl implements TrainerDAO {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Trainer save(Trainer trainer) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(trainer);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Session session = entityManager.unwrap(Session.class);
        return (Trainer) session.merge(trainer);
    }

    @Override
    public Optional<Trainer> findById(Long trainerId) {
        Session session = entityManager.unwrap(Session.class);
        return Optional.ofNullable(session.get(Trainer.class, trainerId));
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Trainer t where t.username = :username", Trainer.class).setParameter("username", username).uniqueResultOptional();
    }

    @Override
    public List<Trainer> findAll() {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Trainer", Trainer.class).list();
    }

    @Override
    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery(
                "select t from Trainer t where t.isActive = true and t.id not in " +
                        "(select tr.id from Trainee e join e.trainers tr where e.username = :username)",
                Trainer.class).setParameter("username", traineeUsername).list();
    }
}
