package org.volodymyrzganiaiko.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.auth_service.client.GymAuthClient;
import org.volodymyrzganiaiko.auth_service.dto.LoginRequest;
import org.volodymyrzganiaiko.auth_service.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.auth_service.service.BruteForceProtectionService;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private GymAuthClient gymAuthClient;
    @Mock
    private TokenService tokenService;
    @Mock
    private BruteForceProtectionService bruteForceProtectionService;
    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String json() throws Exception {
        return objectMapper.writeValueAsString(new LoginRequest("John.Doe", "rawPass"));
    }

    private FeignException.Unauthorized unauthorized() {
        Request request = Request.create(Request.HttpMethod.POST, "/internal/auth/verify",
                Map.of(), null, new RequestTemplate());
        return new FeignException.Unauthorized("Unauthorized", request, null, null);
    }

    @Test
    void login_success() throws Exception {
        when(tokenService.generateToken("John.Doe")).thenReturn("test.token");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.token"));

        verify(bruteForceProtectionService).loginSucceeded("John.Doe");
    }

    @Test
    void login_invalidCredentials_401() throws Exception {
        doThrow(unauthorized()).when(gymAuthClient).verify(any());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json()))
                .andExpect(status().isUnauthorized());

        verify(bruteForceProtectionService).loginFailed("John.Doe");
        verifyNoInteractions(tokenService);
    }

    @Test
    void login_blocked_429() throws Exception {
        when(bruteForceProtectionService.isBlocked("John.Doe")).thenReturn(true);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json()))
                .andExpect(status().isTooManyRequests());

        verifyNoInteractions(gymAuthClient);
    }
}