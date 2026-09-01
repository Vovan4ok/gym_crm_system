package org.volodymyrzganiaiko.gym.crm.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.security.filter.HeaderAuthenticationFilter;

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

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .addFilterBefore(new HeaderAuthenticationFilter(),
                            UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(eh -> eh.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .build();
        }
    }

    @Test
    void validAuthHeader_ok() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe")
                        .header("X-Auth-User", "John.Doe"))
                .andExpect(status().isOk());
    }

    @Test
    void noAuthHeader_401() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void garbageAuthHeader_401() throws Exception {
        mockMvc.perform(get("/api/trainees/{username}", "John.Doe")
                        .header("X-Auth-User", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteOtherUser_forbidden() throws Exception {
        mockMvc.perform(delete("/api/trainees/{username}", "Someone.Else")
                        .header("X-Auth-User", "John.Doe"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(gymFacade);
    }
}