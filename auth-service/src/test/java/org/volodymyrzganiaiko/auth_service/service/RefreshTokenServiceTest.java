package org.volodymyrzganiaiko.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.auth_service.exception.InvalidRefreshTokenException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RefreshTokenServiceTest {
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    public void setUp() {
        this.refreshTokenService = new RefreshTokenService(3600);
    }

    @Test
    public void issue_thenValidate_returnsUsername() {
        String refresh = refreshTokenService.issue("u");
        String username = refreshTokenService.validateAndRotate(refresh);
        assertEquals("u", username);
    }

    @Test
    public void validateAndRotate_rotates() {
        String refresh = refreshTokenService.issue("u");
        refreshTokenService.validateAndRotate(refresh);
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validateAndRotate(refresh));
    }

    @Test
    public void validateAndRotate_unknownToken_throws() {
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validateAndRotate("random"));
    }

    @Test
    public void validateAndRotate_expiredToken_throws() {
        refreshTokenService = new RefreshTokenService(0);
        String token = refreshTokenService.issue("u");
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validateAndRotate(token));
    }

    @Test
    public void purgeExpired_removesExpired() {
        refreshTokenService = new RefreshTokenService(0);
        String token = refreshTokenService.issue("u");
        refreshTokenService.purgeExpired();
        assertThrows(InvalidRefreshTokenException.class, () -> refreshTokenService.validateAndRotate(token));
    }
}
