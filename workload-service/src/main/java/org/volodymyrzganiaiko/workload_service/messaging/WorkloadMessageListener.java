package org.volodymyrzganiaiko.workload_service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

@Component
@Slf4j
public class WorkloadMessageListener {
    private final WorkloadService workloadService;

    public WorkloadMessageListener(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @JmsListener(destination = "${messaging.workload-queue}")
    public void onWorkload(@Payload TrainerWorkloadRequest request,
                           @Header(name = "transactionId", required = false) String transactionId) {
        MDC.put("transactionId", transactionId);
        try {
            log.info("Received workload message: trainer={}, action={}, minutes={}", request.trainerUsername(), request.actionType(), request.trainingDuration());
            workloadService.process(request);
        } finally {
            MDC.clear();
        }
    }
}
