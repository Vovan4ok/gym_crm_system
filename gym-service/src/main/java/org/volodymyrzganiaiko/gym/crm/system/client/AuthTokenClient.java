package org.volodymyrzganiaiko.gym.crm.system.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.volodymyrzganiaiko.gym.crm.system.dto.ServiceTokenRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.ServiceTokenResponse;

@FeignClient(name = "auth-service")
public interface AuthTokenClient {
    @PostMapping("/oauth2/token")
    ServiceTokenResponse getToken(@RequestBody ServiceTokenRequest request);
}
