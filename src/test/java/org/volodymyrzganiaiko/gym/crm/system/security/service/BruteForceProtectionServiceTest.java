package org.volodymyrzganiaiko.gym.crm.system.security.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BruteForceProtectionServiceTest {
    @Test
    public void twoFailures_notBlocked() {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        service.loginFailed("John.Doe");
        service.loginFailed("John.Doe");

        assertFalse(service.isBlocked("John.Doe"));
    }

    @Test
    public void threeFailures_blocked() {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        for (int i = 0; i < 3; i++) {
            service.loginFailed("John.Doe");
        }

        assertTrue(service.isBlocked("John.Doe"));
    }

    @Test
    public void blockExpires_thenReset() throws InterruptedException {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        for (int i = 0; i < 3; i++) {
            service.loginFailed("John.Doe");
        }
        Thread.sleep(60);

        assertFalse(service.isBlocked("John.Doe"));

        service.loginFailed("John.Doe");
        assertFalse(service.isBlocked("John.Doe"));
    }

    @Test
    public void successResetsCounter() {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        service.loginFailed("John.Doe");
        service.loginFailed("John.Doe");
        service.loginSucceeded("John.Doe");
        service.loginFailed("John.Doe");

        assertFalse(service.isBlocked("John.Doe"));
    }

    @Test
    public void purgeStale_removesExpiredEntity() throws InterruptedException {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        service.loginFailed("John.Doe");
        Thread.sleep(60);
        service.purgeStale();

        service.loginFailed("John.Doe");
        service.loginFailed("John.Doe");
        service.loginFailed("John.Doe");
        assertTrue(service.isBlocked("John.Doe"));
    }

    @Test
    public void purgeStale_keepsActiveEntry() {
        BruteForceProtectionService service = new BruteForceProtectionService((byte) 3, 50L);
        service.loginFailed("John.Doe");
        service.purgeStale();
        service.loginFailed("John.Doe");
        service.loginFailed("John.Doe");
        assertTrue(service.isBlocked("John.Doe"));
    }
}
