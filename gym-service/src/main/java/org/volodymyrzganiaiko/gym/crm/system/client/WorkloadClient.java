package org.volodymyrzganiaiko.gym.crm.system.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainerWorkloadRequest;

@FeignClient(name="workload-service")
public interface WorkloadClient {
    @PostMapping("/api/workload")
    void sendWorkload(@RequestBody TrainerWorkloadRequest request);
}
