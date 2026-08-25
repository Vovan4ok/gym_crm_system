package org.volodymyrzganiaiko.gym.crm.system.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@Import(GymSecurityTest.TestSecurityConfig.class)
class GymSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GymFacade gymFacade;

    private static final RSAKey RSA_KEY = generateKey();

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                    .build();
        }

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

    private String mint() {
        JWKSource<SecurityContext> source = (selector, ctx) -> selector.select(new JWKSet(RSA_KEY));
        JwtEncoder encoder = new NimbusJwtEncoder(source);
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("John.Doe")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }

    @Test
    void validToken_ok() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe")
                        .header("Authorization", "Bearer " + mint()))
                .andExpect(status().isOk());
    }

    @Test
    void noToken_401() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void garbageToken_401() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteOtherUser_forbidden() throws Exception {
        mockMvc.perform(delete("/api/trainees/{username}", "Someone.Else")
                        .header("Authorization", "Bearer " + mint()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gymFacade);
    }
}