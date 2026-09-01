package org.volodymyrzganiaiko.gym.crm.system.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.volodymyrzganiaiko.gym.crm.system.dao.OutboxDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.OutboxMessage;
import org.volodymyrzganiaiko.gym.crm.system.dto.ActionType;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxRelayTest {
    @Mock
    private OutboxDAO outboxDAO;

    @Mock
    private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    private OutboxRelay outboxRelay;

    @BeforeEach
    public void setUp() {
        this.outboxRelay = new OutboxRelay(outboxDAO, jmsTemplate, objectMapper, 500);
    }

    @Test
    public void publishPending_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(outboxDAO.findPending(anyInt())).thenReturn(List.of(new OutboxMessage(
                id,
                "queue",
                objectMapper.writeValueAsString(new TrainerWorkloadRequest(
                        "Tra.Iner",
                        "Tra",
                        "Iner",
                        true,
                        LocalDate.now(),
                        60,
                        ActionType.ADD
                )),
                UUID.randomUUID().toString(),
                "Tra.Iner",
                "PENDING",
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        )));

        outboxRelay.publishPending();

        verify(jmsTemplate).convertAndSend(eq("queue"), any(TrainerWorkloadRequest.class), any(MessagePostProcessor.class));
        verify(outboxDAO).markSent(id);
    }

    @Test
    public void publishPending_jmsFail() throws Exception {
        UUID id = UUID.randomUUID();
        OutboxMessage m = new OutboxMessage(
                id, "queue",
                objectMapper.writeValueAsString(new TrainerWorkloadRequest(
                        "Tra.Iner", "Tra", "Iner", true, LocalDate.now(), 60, ActionType.ADD)),
                UUID.randomUUID().toString(), "Tra.Iner", "PENDING", 1,
                LocalDateTime.now(), null);
        when(outboxDAO.findPending(anyInt())).thenReturn(List.of(m));
        doThrow(new RuntimeException("broker down"))
                .when(jmsTemplate).convertAndSend(eq("queue"), any(), any(MessagePostProcessor.class));

        outboxRelay.publishPending();

        verify(outboxDAO, never()).markSent(any());
        assertEquals(2, m.getAttempts());
    }
}
