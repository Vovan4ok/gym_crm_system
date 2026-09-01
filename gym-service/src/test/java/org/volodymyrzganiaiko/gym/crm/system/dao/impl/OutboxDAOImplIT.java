package org.volodymyrzganiaiko.gym.crm.system.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.volodymyrzganiaiko.gym.crm.system.AbstractPostgresIT;
import org.volodymyrzganiaiko.gym.crm.system.dao.DaoTestConfig;
import org.volodymyrzganiaiko.gym.crm.system.dao.OutboxDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.OutboxMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DaoTestConfig.class)
public class OutboxDAOImplIT extends AbstractPostgresIT {
    @Autowired
    private OutboxDAO outboxDAO;

    @PersistenceContext
    private EntityManager entityManager;

    private OutboxMessage outboxMessage;

    @BeforeEach
    public void setUpOutboxMessage() {
        outboxMessage = new OutboxMessage(
                UUID.randomUUID(),
                "test",
                "test",
                "test",
                "test",
                "PENDING",
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void save_success() {
        outboxDAO.save(outboxMessage);
        flushAndClear();

        OutboxMessage result = entityManager.find(OutboxMessage.class, outboxMessage.getId());

        assertEquals(outboxMessage.getId(), result.getId());
        assertEquals(outboxMessage.getDestination(), result.getDestination());
        assertEquals(outboxMessage.getPayload(), result.getPayload());
        assertEquals(outboxMessage.getCorrelationId(), result.getCorrelationId());
        assertEquals(outboxMessage.getGroupId(), result.getGroupId());
        assertEquals(outboxMessage.getStatus(), result.getStatus());
        assertEquals(outboxMessage.getAttempts(), result.getAttempts());
    }

    @Test
    public void findPending_success() {
        outboxDAO.save(outboxMessage);
        flushAndClear();

        List<OutboxMessage> found = outboxDAO.findPending(1);
        OutboxMessage result = found.get(0);

        assertEquals(outboxMessage.getId(), result.getId());
        assertEquals(outboxMessage.getDestination(), result.getDestination());
        assertEquals(outboxMessage.getPayload(), result.getPayload());
        assertEquals(outboxMessage.getCorrelationId(), result.getCorrelationId());
        assertEquals(outboxMessage.getGroupId(), result.getGroupId());
        assertEquals(outboxMessage.getStatus(), result.getStatus());
        assertEquals(outboxMessage.getAttempts(), result.getAttempts());
    }

    @Test
    public void markSent_success() {
        OutboxMessage saveResult = outboxDAO.save(outboxMessage);
        flushAndClear();

        outboxDAO.markSent(saveResult.getId());

        List<OutboxMessage> found = outboxDAO.findPending(1);
        assertTrue(found.isEmpty());
    }
}
