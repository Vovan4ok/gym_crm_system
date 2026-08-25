package org.volodymyrzganiaiko.auth_service.controller;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwksControllerTest {
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

    @Test
    @SuppressWarnings("unchecked")
    void exposesPublicKeyWithoutPrivateFields() {
        RSAKey rsaKey = generateKey();
        Map<String, Object> result = new JwksController(rsaKey).getKeySet();

        List<Map<String, Object>> keys = (List<Map<String, Object>>) result.get("keys");
        assertEquals(1, keys.size());
        Map<String, Object> key = keys.get(0);

        assertEquals("RSA", key.get("kty"));
        assertNotNull(key.get("kid"));
        assertNotNull(key.get("n"));
        assertNotNull(key.get("e"));

        for (String priv : List.of("d", "p", "q", "dp", "dq", "qi")) {
            assertFalse(key.containsKey(priv), "public JWKS must not expose " + priv);
        }
    }
}
