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
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        when(workloadService.getWorkload("x"))
                .thenReturn(new TrainerSummaryResponse("x", "Tra", "Iner", true, List.of()));
        String token = mint(Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/api/workload/x")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        verify(workloadService).getWorkload("x");
    }

    @Test
    void noToken_401() throws Exception {
        mockMvc.perform(get("/api/workload/x"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }

    @Test
    void garbageToken_401() throws Exception {
        mockMvc.perform(get("/api/workload/x")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }

    @Test
    void expiredToken_401() throws Exception {
        String token = mint(Instant.now().minusSeconds(60));
        mockMvc.perform(get("/api/workload/x")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(workloadService);
    }
}