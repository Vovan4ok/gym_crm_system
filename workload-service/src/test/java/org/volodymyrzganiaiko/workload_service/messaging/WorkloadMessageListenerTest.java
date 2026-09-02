package org.volodymyrzganiaiko.workload_service.messaging;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.volodymyrzganiaiko.workload_service.dto.ActionType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageListenerTest {
    @Mock
    WorkloadService workloadService;
    @Mock
    JmsTemplate jmsTemplate;
    @Mock
    ProcessMessageStore processMessageStore;

    WorkloadMessageListener listener;

    @BeforeEach
    public void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        listener = new WorkloadMessageListener(workloadService, jmsTemplate, validator, "gym.workload.dlq", processMessageStore);
    }

    @Test
    public void validMessage_isProcessed() {
        var req = new TrainerWorkloadRequest("Tra.Iner", "Tra", "Iner", true,
                LocalDate.parse("2026-08-01"), 60, ActionType.ADD);

        listener.onWorkload(req, "tx-1", "m-1");

        verify(workloadService).process(req);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void invalidMessage_goesToDlq() {
        var bad = new TrainerWorkloadRequest("  ", "Tra", "Iner", true,
                LocalDate.parse("2026-08-01"), 60, ActionType.ADD); // blank username

        listener.onWorkload(bad, "tx-1", "m-1");

        verify(jmsTemplate).convertAndSend(eq("gym.workload.dlq"), eq(bad), any(MessagePostProcessor.class));
        verifyNoInteractions(workloadService);
    }

    @Test
    public void duplicateMessageId_processedOnce() {
        when(processMessageStore.isProcessed("m-1")).thenReturn(false, true);
        var req = validReq();
        listener.onWorkload(req, "tx-1", "m-1");
        listener.onWorkload(req, "tx-1", "m-1");
        verify(workloadService, times(1)).process(req);
    }

    @Test
    public void distinctMessageIds_processedEach() {
        var req = validReq();
        listener.onWorkload(req, "tx-1", "m-1");
        listener.onWorkload(req, "tx-1", "m-2");
        verify(workloadService, times(2)).process(req);
    }

    @Test
    public void nullMessageId_alwaysProcessed() {
        var req = validReq();
        listener.onWorkload(req, "tx-1", null);
        listener.onWorkload(req, "tx-1", null);
        verify(workloadService, times(2)).process(req);
    }

    @Test
    public void processFailure_doesNotBurnId_reprocessable() {
        var req = validReq();
        doThrow(new RuntimeException("boom")).doNothing().when(workloadService).process(req);

        assertThrows(RuntimeException.class, () -> listener.onWorkload(req, "tx-1", "m-1"));
        listener.onWorkload(req, "tx-1", "m-1");

        verify(workloadService, times(2)).process(req);
    }

    private TrainerWorkloadRequest validReq() {
        return new TrainerWorkloadRequest("Tra.Iner", "Tra", "Iner", true,
                LocalDate.parse("2026-08-01"), 60, ActionType.ADD);
    }
}
