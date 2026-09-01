package org.volodymyrzganiaiko.gym.crm.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@Import(TraineeControllerSecurityTest.MethodSecurityTestConfig.class)
class TraineeControllerSecurityTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GymFacade gymFacade;

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
        @Bean
        SecurityFilterChain chain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void changeStatus_otherUser_forbidden() throws Exception {
        mockMvc.perform(patch("/api/trainees/{username}/status", "Tr.Ainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}")
                        .header("X-Auth-User", "Someone.Else"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void changeStatus_owner_ok() throws Exception {
        mockMvc.perform(patch("/api/trainees/{username}/status", "Tr.Ainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isActive\":false}")
                        .header("X-Auth-User", "Tr.Ainee"))
                .andExpect(status().isOk());

        verify(gymFacade).changeTraineeStatus("Tr.Ainee", false);
    }

}
