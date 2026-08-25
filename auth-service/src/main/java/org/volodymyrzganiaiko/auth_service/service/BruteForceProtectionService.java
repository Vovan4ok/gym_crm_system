package org.volodymyrzganiaiko.auth_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {
    private static class UserLoginStatistic {
        private byte loginAttempts;
        private long blockedUntil;
        private long lastFailureMs;

        public UserLoginStatistic(byte loginAttempts, long blockedUntil, long lastFailureMs) {
            this.loginAttempts = loginAttempts;
            this.blockedUntil = blockedUntil;
            this.lastFailureMs = lastFailureMs;
        }
    }

    private final ConcurrentHashMap<String, UserLoginStatistic> memory;
    private final byte maxAttempts;
    private final long durationMs;

    public BruteForceProtectionService(@Value("${security.brute-force.max-attempts}") byte maxAttempts, @Value("${security.brute-force.block-duration-ms}") long durationMs) {
        this.maxAttempts = maxAttempts;
        this.durationMs = durationMs;
        this.memory = new ConcurrentHashMap<>();
    }

    public boolean isBlocked(String username) {
        long now = System.currentTimeMillis();
        UserLoginStatistic stat = memory.get(username);
        if (stat == null || stat.blockedUntil == 0) return false;
        if (now >= stat.blockedUntil) {
            loginSucceeded(username);
            return false;
        }
        return true;
    }

    public void loginFailed(String username) {
        memory.compute(username, (key, stat) -> {
            long now = System.currentTimeMillis();
            if (stat == null) {
                return new UserLoginStatistic((byte) 1, 0, now);
            }
            stat.loginAttempts += 1;
            stat.lastFailureMs = now;
            if (stat.loginAttempts == maxAttempts) {
                stat.blockedUntil = now + durationMs;
            }
            return stat;
        });
    }

    public void loginSucceeded(String username) {
        memory.remove(username);
    }

    @Scheduled(fixedDelayString = "${security.brute-force.cleanup-interval-ms}")
    public void purgeStale() {
        long now = System.currentTimeMillis();
        memory.entrySet().removeIf(e -> now - e.getValue().lastFailureMs > durationMs);
    }
}
