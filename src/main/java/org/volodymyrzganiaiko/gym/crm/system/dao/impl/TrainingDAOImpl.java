package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.TrainingDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Training;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Training save(Training training) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(training);
        return training;
    }

    @Override
    public Optional<Training> findById(Long trainingId) {
        Session session = entityManager.unwrap(Session.class);
        return Optional.ofNullable(session.get(Training.class, trainingId));
    }

    @Override
    public List<Training> findAll() {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from Training", Training.class).list();
    }

    @Override
    public List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate, String trainerUsername, String trainingTypeName) {
        Session session = entityManager.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Training> cr =  cb.createQuery(Training.class);
        Root<Training> root = cr.from(Training.class);
        List<Predicate> predicates = buildPredicates(cb, root, traineeUsername, trainerUsername, fromDate, toDate, trainingTypeName);
        cr.select(root).where(predicates.toArray(new Predicate[0]));
        return session.createQuery(cr).getResultList();
    }

    @Override
    public List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate, String traineeUsername) {
        Session session = entityManager.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Training> cr =  cb.createQuery(Training.class);
        Root<Training> root = cr.from(Training.class);
        List<Predicate> predicates = buildPredicates(cb, root, traineeUsername, trainerUsername, fromDate, toDate, null);
        cr.select(root).where(predicates.toArray(new Predicate[0]));
        return session.createQuery(cr).getResultList();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Training> root,
                                            String traineeUsername, String trainerUsername, LocalDate from, LocalDate to, String trainingTypeName) {
        List<Predicate> predicates = new ArrayList<>();
        if (traineeUsername != null) {
            predicates.add(cb.equal(root.get("trainee").get("username"), traineeUsername));
        }
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), to));
        }
        if (trainerUsername != null) {
            predicates.add(cb.equal(root.get("trainer").get("username"), trainerUsername));
        }
        if (trainingTypeName != null) {
            predicates.add(cb.equal(root.get("trainingType").get("trainingTypeName"), trainingTypeName));
        }
        return predicates;
    }
}
