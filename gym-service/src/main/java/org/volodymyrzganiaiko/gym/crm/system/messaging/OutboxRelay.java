package org.volodymyrzganiaiko.gym.crm.system.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.volodymyrzganiaiko.gym.crm.system.dao.OutboxDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.OutboxMessage;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

import java.util.List;

@Component
@Slf4j
public class OutboxRelay {
    private final OutboxDAO outboxDAO;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;

    public OutboxRelay(OutboxDAO outboxDAO, JmsTemplate jmsTemplate, ObjectMapper objectMapper, @Value("${messaging.outbox.batch-size}") int batchSize) {
        this.outboxDAO = outboxDAO;
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${messaging.outbox.poll-delay}")
    @Transactional
    public void publishPending() {
        List<OutboxMessage> batch = outboxDAO.findPending(batchSize);
        for (OutboxMessage m : batch) {
            try {
                TrainerWorkloadRequest req = objectMapper.readValue(m.getPayload(), TrainerWorkloadRequest.class);
                jmsTemplate.convertAndSend(m.getDestination(), req, message -> {
                    message.setStringProperty("correlationId", m.getCorrelationId());
                    message.setStringProperty("JMSXGroupID", m.getGroupId());
                    message.setStringProperty("messageId", m.getId().toString());
                    return message;
                });
                outboxDAO.markSent(m.getId());
            } catch (Exception e) {
                log.warn("Failed to publish outbox message {}, will retry", m.getId(), e);
                m.setAttempts(m.getAttempts() + 1);
            }
        }
    }
}
