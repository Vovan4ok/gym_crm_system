package org.volodymyrzganiaiko.gym.crm.system.listener;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.volodymyrzganiaiko.gym.crm.system.event.WorkloadNotificationEvent;

@Component
@Slf4j
public class WorkloadNotificationListener {
    private final JmsTemplate jmsTemplate;
    private final String queue;

    public WorkloadNotificationListener(JmsTemplate jmsTemplate,
                                        @Value("${messaging.workload-queue}") String queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    @Async("workloadExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkloadNotification(WorkloadNotificationEvent event) {
        MDC.put("transactionId", event.transactionId());
        try {
            log.info("Sending {} workload messages after commit", event.requests().size());
            event.requests().forEach(request ->
                    jmsTemplate.convertAndSend(queue, request, message -> {
                        message.setStringProperty("transactionId", event.transactionId());
                        return message;
                    }));
        } finally {
            MDC.clear();
        }
    }
}
