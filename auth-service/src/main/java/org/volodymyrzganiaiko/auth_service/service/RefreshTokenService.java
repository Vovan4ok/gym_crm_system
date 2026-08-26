package org.volodymyrzganiaiko.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.auth_service.exception.InvalidRefreshTokenException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {
    private record Entry(String username, long expiresAtMs) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final long refreshExpirationSeconds;

    public RefreshTokenService(@Value("${jwt.refresh-expiration-seconds}") long refreshExpirationSeconds) {
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String issue(String username) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        store.put(token, new Entry(username, System.currentTimeMillis() + refreshExpirationSeconds * 1000));
        return token;
    }

    public String validateAndRotate(String refreshToken) {
        Entry entry = store.remove(refreshToken);
        if (entry == null) {
            throw new InvalidRefreshTokenException("Refresh token is invalid");
        }
        if (entry.expiresAtMs < System.currentTimeMillis()) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }
        return entry.username();
    }

    @Scheduled(fixedRateString = "${jwt.refresh-cleanup-interval-ms}")
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        store.values().removeIf(entry -> entry.expiresAtMs() < now);
    }
}
