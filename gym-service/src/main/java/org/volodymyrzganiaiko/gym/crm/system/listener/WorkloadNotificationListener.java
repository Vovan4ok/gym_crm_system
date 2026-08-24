package org.volodymyrzganiaiko.gym.crm.system.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.volodymyrzganiaiko.gym.crm.system.client.WorkloadClient;
import org.volodymyrzganiaiko.gym.crm.system.event.TraineeDeletedWorkloadEvent;

@Component
public class WorkloadNotificationListener {
    private final static Logger log = LoggerFactory.getLogger(WorkloadNotificationListener.class);
    private final WorkloadClient workloadClient;

    public WorkloadNotificationListener(WorkloadClient workloadClient) {
        this.workloadClient = workloadClient;
    }

    @Async("workloadExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTraineeDeleted(TraineeDeletedWorkloadEvent event) {
        MDC.put("transactionId", event.transactionId());
        try {
            log.info("Sending {} workload notifications after commit", event.workloads().size());
            event.workloads().forEach(workload -> workloadClient.sendWorkload(workload, event.transactionId()));
        } finally {
            MDC.clear();
        }
    }
}
