package org.volodymyrzganiaiko.workload_service.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "eureka.client.enabled=false")
public class WorkloadSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkloadService workloadService;

    private static final RSAKey RSA_KEY = generateKey();

    private static final String BODY = """
              {"trainerUsername":"Tra.Iner","firstName":"Tra","lastName":"Iner",\
              "isActive":true,"trainingDate":"2026-09-01","trainingDuration":60,"actionType":"ADD"}""";

    @TestConfiguration
    static class TestDecoderConfig {
        @Bean
        JwtDecoder jwtDecoder() throws Exception {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) RSA_KEY.toPublicKey()).build();
        }
    }

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

    private String mint(Instant expiresAt) {
        JWKSource<SecurityContext> source = (selector, ctx) -> selector.select(new JWKSet(RSA_KEY));
        JwtEncoder encoder = new NimbusJwtEncoder(source);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("gym-service")
                .issuedAt(expiresAt.minusSeconds(3600))
                .expiresAt(expiresAt)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims
        )).getTokenValue();
    }

    @Test
    void validToken_ok() throws Exception {
        String token = mint(Instant.now().plusSeconds(3600));
        mockMvc.perform(post("/api/workload")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk());
        verify(workloadService).process(any());
    }

    @Test
    void noToken_401() throws Exception {
        mockMvc.perform(post("/api/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }

    @Test
    void garbageToken_401() throws Exception {
        mockMvc.perform(post("/api/workload")
                        .header("Authorization", "Bearer not.a.jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }

    @Test
    void expiredToken_401() throws Exception {
        String token = mint(Instant.now().minusSeconds(60));
        mockMvc.perform(post("/api/workload")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }
}
