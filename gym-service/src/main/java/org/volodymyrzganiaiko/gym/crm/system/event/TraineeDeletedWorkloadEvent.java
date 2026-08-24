package org.volodymyrzganiaiko.gym.crm.system.event;

import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

import java.util.List;

public record TraineeDeletedWorkloadEvent(List<TrainerWorkloadRequest> workloads, String transactionId) {
}
