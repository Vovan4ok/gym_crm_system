package org.volodymyrzganiaiko.workload_service.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WorkloadMessageListener {
    private final WorkloadService workloadService;
    private final JmsTemplate jmsTemplate;
    private final Validator validator;
    private final String dlqQueue;
    private final ProcessMessageStore processMessageStore;

    public WorkloadMessageListener(WorkloadService workloadService, JmsTemplate jmsTemplate,
                                   Validator validator, @Value("${messaging.workload-dlq}") String dlqQueue, ProcessMessageStore processMessageStore) {
        this.workloadService = workloadService;
        this.jmsTemplate = jmsTemplate;
        this.validator = validator;
        this.dlqQueue = dlqQueue;
        this.processMessageStore = processMessageStore;
    }

    @JmsListener(destination = "${messaging.workload-queue}",
    concurrency = "${messaging.workload-concurrency}")
    public void onWorkload(@Payload TrainerWorkloadRequest request,
                           @Header(name = "correlationId", required = false) String correlationId,
                           @Header(name = "messageId", required = false) String messageId) {
        MDC.put("correlationId", correlationId);
        try {
            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String reason = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(Collectors.joining("; "));
                log.warn("Invalid workload message, routing to DLQ: {}", reason);
                jmsTemplate.convertAndSend(dlqQueue, request, message -> {
                    message.setStringProperty("correlationId", correlationId);
                    message.setStringProperty("dlqReason", reason);
                    return message;
                });
                return;
            }
            if (messageId != null && processMessageStore.isProcessed(messageId)) {
                log.info("Duplicate workload message {}, skipping", messageId);
                return;
            }
            log.info("Received workload message: trainer={}, action={}, minutes={}",
                    request.trainerUsername(), request.actionType(), request.trainingDuration());
            workloadService.process(request);
            if (messageId != null) {
                processMessageStore.markProcessed(messageId);
            }
        } finally {
            MDC.clear();
        }
    }
}
