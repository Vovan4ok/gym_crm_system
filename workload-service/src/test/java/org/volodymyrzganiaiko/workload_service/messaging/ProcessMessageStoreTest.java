package org.volodymyrzganiaiko.workload_service.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.workload_service.domain.ProcessedMessage;
import org.volodymyrzganiaiko.workload_service.repository.ProcessedMessageRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        when(processedMessageRepository.existsById("id")).thenReturn(Mono.just(true));

        assertEquals(Boolean.TRUE, processMessageStore.isProcessed("id").block());
    }

    @Test
    public void markProcessed() {
        when(processedMessageRepository.save(any())).thenReturn(Mono.just(new ProcessedMessage("id", Instant.now())));
        processMessageStore.markProcessed("id").block();

        ArgumentCaptor<ProcessedMessage> captor = ArgumentCaptor.forClass(ProcessedMessage.class);
        verify(processedMessageRepository).save(captor.capture());
        assertEquals("id", captor.getValue().getMessageId());
    }
}
