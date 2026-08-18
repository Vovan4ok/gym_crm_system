package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import org.hibernate.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Optional<User> findByUsername(String username) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from User u where u.username = :username", User.class).setParameter("username", username).uniqueResultOptional();
    }

    @Override
    public List<User> findAll() {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from User", User.class).list();
    }
}
