package org.volodymyrzganiaiko.gym.crm.system.client;

import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadResponse;

import java.util.Optional;

/**
 * Queries workload-service. The facade depends on this interface only, so no
 * gRPC types leak outside the client package and the transport can change.
 */
public interface WorkloadClient {

    /**
     * @return the trainer's workload, or empty if workload-service has none
     *         (the trainer has no trainings yet)
     * @throws WorkloadUnavailableException if workload-service cannot answer
     */
    Optional<TrainerWorkloadResponse> findWorkload(String trainerUsername);
}
