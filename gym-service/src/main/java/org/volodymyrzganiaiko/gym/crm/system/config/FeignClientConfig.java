package org.volodymyrzganiaiko.gym.crm.system.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.volodymyrzganiaiko.gym.crm.system.security.service.JwtService;

@Configuration
public class FeignClientConfig {
    private final JwtService jwtService;

    public FeignClientConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public RequestInterceptor workloadAuthInterceptor() {
        return template -> template.header("Authorization",
                "Bearer " + jwtService.generateToken("gym-service"));
    }
}
