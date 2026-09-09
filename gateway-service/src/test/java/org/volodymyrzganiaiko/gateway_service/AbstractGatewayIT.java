package org.volodymyrzganiaiko.gateway_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class AbstractGatewayIT {
    public static final RSAKey RSA_KEY = generateKey();

    public static final WireMockServer WIREMOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        WIREMOCK.start();
        WIREMOCK.stubFor(get(urlEqualTo("/oauth2/jwks"))
                .willReturn(okJson(new JWKSet(RSA_KEY.toPublicJWK()).toString())));
        WIREMOCK.stubFor(any(urlPathMatching("/api/.*")).willReturn(ok()));
    }

    private static RSAKey generateKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyID("test-key")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        String base = WIREMOCK.baseUrl();
        registry.add("AUTH_JWKS_URI", () -> base + "/oauth2/jwks");
        registry.add("GYM_URI", () -> base);
        registry.add("WORKLOAD_URI", () -> base);
        registry.add("AUTH_URI", () -> base);
    }
}
