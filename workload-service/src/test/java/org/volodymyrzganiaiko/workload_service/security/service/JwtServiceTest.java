package org.volodymyrzganiaiko.workload_service.security.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtServiceTest {
    private static final String SECRET = "test-secret-that-is-long-enough-for-hmac-sha256-xx";
    private final JwtService jwtService = new JwtService(SECRET);

    private String tokenSignedWith(String secret, String subject) {
        return Jwts.builder().subject(subject)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    public void extractSubject_validToken() {
        String token = tokenSignedWith(SECRET, "gym-service");
        String result = jwtService.extractSubject(token);

        assertEquals("gym-service", result);
    }

    @Test
    public void extractSubject_wrongSignature() {
        String token = tokenSignedWith("another-secret-also-long-enough-for-hmac-256!!", "gym-service");

        assertThrows(JwtException.class, () -> jwtService.extractSubject(token));
    }

    @Test
    public void extractSubject_malformed() {
        String token = "garbage.token";

        assertThrows(JwtException.class, () -> jwtService.extractSubject(token));
    }
}
