package org.volodymyrzganiaiko.gym.crm.system.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.volodymyrzganiaiko.gym.crm.system.service.TokenProviderService;

public class FeignClientConfig {
    private final TokenProviderService tokenProviderService;

    public FeignClientConfig(TokenProviderService tokenProviderService) {
        this.tokenProviderService = tokenProviderService;
    }

    @Bean
    public RequestInterceptor workloadAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + tokenProviderService.getToken());
    }
}
