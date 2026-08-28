package org.volodymyrzganiaiko.gym.crm.system.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.volodymyrzganiaiko.gym.crm.system.dto.ActionType;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.gym.crm.system.event.WorkloadNotificationEvent;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadNotificationListenerTest {
    @Mock
    JmsTemplate jmsTemplate;
    WorkloadNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new WorkloadNotificationListener(jmsTemplate, "gym.workload.queue");
    }

    @Test
    void sendsEachWorkload() {
        var w1 = new TrainerWorkloadRequest("Tra.Iner", "Tra", "Iner", true,
                LocalDate.parse("2024-01-10"), 60, ActionType.DELETE);

        listener.onWorkloadNotification(new WorkloadNotificationEvent(List.of(w1), "txn-123"));

        verify(jmsTemplate).convertAndSend(eq("gym.workload.queue"), eq(w1), any(MessagePostProcessor.class));
    }

    @Test
    void emptyList_sendsNothing() {
        listener.onWorkloadNotification(new WorkloadNotificationEvent(List.of(), "txn-123"));
        verifyNoInteractions(jmsTemplate);
    }
}