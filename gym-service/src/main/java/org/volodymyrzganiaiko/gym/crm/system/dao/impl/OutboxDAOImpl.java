package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.volodymyrzganiaiko.gym.crm.system.dao.OutboxDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.OutboxMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxDAOImpl implements OutboxDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public OutboxMessage save(OutboxMessage message) {
        Session session = entityManager.unwrap(Session.class);
        session.persist(message);
        return message;
    }

    @Override
    public List<OutboxMessage> findPending(int limit) {
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery("from OutboxMessage m where m.status='PENDING' order by m.createdAt", OutboxMessage.class)
                .setMaxResults(limit)
                .list();
    }

    @Override
    public void markSent(UUID id) {
        Session session = entityManager.unwrap(Session.class);
        OutboxMessage message = session.get(OutboxMessage.class, id);
        message.setStatus("SENT");
        message.setSentAt(LocalDateTime.now());
    }
}
