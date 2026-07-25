package org.volodymyrzganiaiko.gym.crm.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.gym.crm.system.dto.ChangePasswordRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.Credentials;
import org.volodymyrzganiaiko.gym.crm.system.exception.AuthenticationException;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.gym.crm.system.resolver.CredentialsArgumentResolver;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private GymFacade gymFacade;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .build();
    }

    @Test
    public void login_success() throws Exception {
        mockMvc.perform(get("/api/login")
                        .header("X-Username", "John.Doe")
                        .header("X-Password", "random"))
                .andExpect(status().isOk());

        ArgumentCaptor<Credentials> captor = ArgumentCaptor.forClass(Credentials.class);
        verify(gymFacade).login(captor.capture());

        Credentials credentials = captor.getValue();
        assertEquals("John.Doe", credentials.username());
        assertEquals("random", credentials.password());
    }

    @Test
    public void login_invalidCredentials() throws Exception {
        doThrow(new AuthenticationException("Wrong username or password"))
                .when(gymFacade).login(any());

        mockMvc.perform(get("/api/login")
                        .header("X-Username", "John.Doe")
                        .header("X-Password", "random"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Wrong username or password"))
                .andExpect(content().string(not(containsString("random"))));
    }

    @Test
    public void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("newPass");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/login")
                        .header("X-Username", "John.Doe")
                        .header("X-Password", "random")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(gymFacade).changeLogin(new Credentials("John.Doe", "random"), "newPass");
    }

    @Test
    public void changePassword_emptyPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/login")
                        .header("X-Username", "John.Doe")
                        .header("X-Password", "random")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("newPassword")));
        verifyNoInteractions(gymFacade);
    }
}
