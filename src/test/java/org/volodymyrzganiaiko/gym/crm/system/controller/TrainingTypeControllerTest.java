package org.volodymyrzganiaiko.gym.crm.system.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.gym.crm.system.dto.Credentials;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainingTypeResponse;
import org.volodymyrzganiaiko.gym.crm.system.exception.AuthenticationException;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.gym.crm.system.resolver.CredentialsArgumentResolver;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainingTypeControllerTest {
    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private TrainingTypeController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .build();
    }

    @Test
    public void getTrainingTypes_returnsList() throws Exception {
        when(gymFacade.getTrainingTypes(any()))
                .thenReturn(List.of(new TrainingTypeResponse(1L, "Yoga"),
                        new TrainingTypeResponse(2L, "Cardio")));

        mockMvc.perform(get("/api/training-types")
                        .header("X-Username", "Tr.Ainee")
                        .header("X-Password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Yoga"));
    }

    @Test
    public void getTrainingTypes_passesCredentialsFromHeaders() throws Exception {
        when(gymFacade.getTrainingTypes(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/training-types")
                .header("X-Username", "John.Doe")
                .header("X-Password", "random"))
                .andExpect(status().isOk());

        ArgumentCaptor<Credentials> captor = ArgumentCaptor.forClass(Credentials.class);
        verify(gymFacade).getTrainingTypes(captor.capture());
        Credentials credentials = captor.getValue();
        assertEquals("John.Doe", credentials.username());
        assertEquals("random", credentials.password());
    }

    @Test
    public void getTrainingTypes_throwsAuthenticationException() throws Exception {
        when(gymFacade.getTrainingTypes(any()))
                .thenThrow(new AuthenticationException("Wrong username or password"));

        mockMvc.perform(get("/api/training-types")
                        .header("X-Username", "John.Doe")
                        .header("X-Password", "random"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Wrong username or password"));
    }
}
