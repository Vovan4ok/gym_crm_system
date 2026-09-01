package org.volodymyrzganiaiko.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.volodymyrzganiaiko.auth_service.dto.LoginRequest;

@FeignClient(name = "gym-crm-system", url = "${gym-service.url}")
public interface GymAuthClient {
    @PostMapping("/internal/auth/verify")
    void verify(@RequestBody LoginRequest loginRequest);
}
