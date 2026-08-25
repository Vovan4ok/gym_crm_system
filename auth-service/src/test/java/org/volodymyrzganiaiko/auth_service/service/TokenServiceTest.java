package org.volodymyrzganiaiko.auth_service.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenServiceTest {
    private static final RSAKey RSA_KEY = generateKey();

    private static RSAKey generateKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey(kp.getPrivate())
                    .keyID("test-key")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TokenService tokenService() {
        JWKSource<SecurityContext> source = (sel, ctx) -> sel.select(new JWKSet(RSA_KEY));
        return new TokenService(new NimbusJwtEncoder(source), 3600);
    }

    private Jwt decode(String token) throws Exception {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) RSA_KEY.toPublicKey()).build().decode(token);
    }

    @Test
    public void token_hasSubject() throws Exception {
        assertEquals("John.Doe", decode(tokenService().generateToken("John.Doe")).getSubject());
    }

    @Test
    public void token_expiresAfterConfiguredSeconds() throws Exception {
        Jwt jwt = decode(tokenService().generateToken("John.Doe"));

        assertNotNull(jwt.getIssuedAt());
        assertEquals(3600, Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).getSeconds());
    }

    @Test
    public void token_isRs256WithKeyId() throws Exception {
        Jwt jwt = decode(tokenService().generateToken("John.Doe"));

        assertEquals("RS256", jwt.getHeaders().get("alg"));
        assertEquals("test-key", jwt.getHeaders().get("kid"));
    }
}
