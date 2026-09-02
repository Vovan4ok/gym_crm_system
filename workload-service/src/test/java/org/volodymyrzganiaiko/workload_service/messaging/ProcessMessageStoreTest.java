package org.volodymyrzganiaiko.workload_service.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.workload_service.domain.ProcessedMessage;
import org.volodymyrzganiaiko.workload_service.repository.ProcessedMessageRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProcessMessageStoreTest {
    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private ProcessMessageStore processMessageStore;

    @Test
    public void isProcessed() {
        when(processedMessageRepository.existsById("id")).thenReturn(true);

        assertTrue(processMessageStore.isProcessed("id"));
    }

    @Test
    public void markProcessed() {
        processMessageStore.markProcessed("id");

        ArgumentCaptor<ProcessedMessage> captor = ArgumentCaptor.forClass(ProcessedMessage.class);
        verify(processedMessageRepository).save(captor.capture());
        assertEquals("id", captor.getValue().getMessageId());
    }
}
