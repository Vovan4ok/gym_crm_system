package org.volodymyrzganiaiko.gym.crm.system.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.client.WorkloadClient;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

@Component
public class WorkloadClientFallback implements WorkloadClient {
    private static final Logger log = LoggerFactory.getLogger(WorkloadClientFallback.class);

    @Override
    public void sendWorkload(TrainerWorkloadRequest request, String transactionId) {
        log.warn("Workload service unavailable — skipped {} event for trainer {}",
                request.actionType(), request.trainerUsername());
    }
}
