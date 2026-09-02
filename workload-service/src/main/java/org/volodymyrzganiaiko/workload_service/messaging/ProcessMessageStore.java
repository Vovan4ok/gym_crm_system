package org.volodymyrzganiaiko.workload_service.messaging;

import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.workload_service.domain.ProcessedMessage;
import org.volodymyrzganiaiko.workload_service.repository.ProcessedMessageRepository;

import java.time.Instant;

@Component
public class ProcessMessageStore {
    private final ProcessedMessageRepository repository;

    public ProcessMessageStore(ProcessedMessageRepository repository) {
        this.repository = repository;
    }

    public boolean isProcessed(String messageId) {
        return repository.existsById(messageId);
    }

    public void markProcessed(String messageId) {
        repository.save(new ProcessedMessage(messageId, Instant.now()));
    }
}
