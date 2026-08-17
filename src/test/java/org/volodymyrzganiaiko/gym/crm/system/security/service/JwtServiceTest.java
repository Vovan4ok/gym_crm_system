package org.volodymyrzganiaiko.gym.crm.system.security.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService("test-secret-that-is-long-enough-32chars", 3600000L);
    }

    @Test
    public void extractUsername_extractsCorrect() {
        String username = jwtService.extractUsername(jwtService.generateToken("John.Doe"));

        assertEquals("John.Doe", username);
    }

    @Test
    public void generateTokensForDifferentUsernames() {
        String token1 = jwtService.generateToken("John.Doe");
        String token2 = jwtService.generateToken("Jane.Smith");

        assertNotEquals(token1, token2);
        assertEquals("John.Doe", jwtService.extractUsername(token1));
        assertEquals("Jane.Smith", jwtService.extractUsername(token2));
    }

    @Test
    public void unknownSecretGetsRejected() {
        JwtService otherService = new JwtService("another-different-secret-32chars-long!!", 3600000L);

        String token = otherService.generateToken("John.Doe");

        assertThrows(JwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    public void expiredSecretGetsRejected() {
        JwtService otherService = new JwtService("test-secret-that-is-long-enough-32chars", -1000L);

        String token = otherService.generateToken("John.Doe");

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }
}
