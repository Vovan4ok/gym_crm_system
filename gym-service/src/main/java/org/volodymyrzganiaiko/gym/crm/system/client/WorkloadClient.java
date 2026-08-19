package org.volodymyrzganiaiko.gym.crm.system.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.volodymyrzganiaiko.gym.crm.system.client.impl.WorkloadClientFallback;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

@FeignClient(name="workload-service", fallback = WorkloadClientFallback.class)
public interface WorkloadClient {
    @PostMapping("/api/workload")
    void sendWorkload(@RequestBody TrainerWorkloadRequest request);
}
