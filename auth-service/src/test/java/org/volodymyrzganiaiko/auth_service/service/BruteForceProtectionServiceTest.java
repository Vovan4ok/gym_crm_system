package org.volodymyrzganiaiko.auth_service.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BruteForceProtectionServiceTest {
    private final BruteForceProtectionService bruteForceProtectionService = new BruteForceProtectionService((byte) 3, 300000);

    @Test
    public void notBlockedInitially() {
        assertFalse(bruteForceProtectionService.isBlocked("x"));
    }

    @Test
    public void blockedAfterMaxAttempts() {
        for (int i = 0; i < 3; i++) {
            bruteForceProtectionService.loginFailed("x");
        }
        assertTrue(bruteForceProtectionService.isBlocked("x"));
    }

    @Test
    public void belowThresholdNotBlocked() {
        for (int i = 0; i < 2; i++) {
            bruteForceProtectionService.loginFailed("x");
        }
        assertFalse(bruteForceProtectionService.isBlocked("x"));
    }

    @Test
    public void successResets() {
        for (int i = 0; i < 2; i++) {
            bruteForceProtectionService.loginFailed("x");
        }
        bruteForceProtectionService.loginSucceeded("x");
        for (int i = 0; i < 2; i++) {
            bruteForceProtectionService.loginFailed("x");
        }
        assertFalse(bruteForceProtectionService.isBlocked("x"));
    }

    @Test
    public void perUserIsolation() {
        for (int i = 0; i < 3; i++) {
            bruteForceProtectionService.loginFailed("x");
        }
        assertFalse(bruteForceProtectionService.isBlocked("u"));
    }
}
