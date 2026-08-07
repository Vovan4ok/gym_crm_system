package org.volodymyrzganiaiko.gym.crm.system.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenDenylistService {
    private final ConcurrentHashMap<String, Long> state;
    private final JwtService jwtService;

    public TokenDenylistService(JwtService jwtService) {
        this.jwtService = jwtService;
        state = new ConcurrentHashMap<>();
    }

    public void deny(String token) {
        state.put(token, jwtService.extractExpiration(token));
    }

    public boolean isDenied(String token) {
        return state.containsKey(token);
    }

    @Scheduled(fixedDelayString = "${security.logout.cleanup-interval-ms}")
    public void purgeExpired() {
        state.entrySet().removeIf(e -> e.getValue() < System.currentTimeMillis());
    }
}
