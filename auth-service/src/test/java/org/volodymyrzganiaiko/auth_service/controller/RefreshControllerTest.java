package org.volodymyrzganiaiko.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.auth_service.dto.RefreshRequest;
import org.volodymyrzganiaiko.auth_service.exception.InvalidRefreshTokenException;
import org.volodymyrzganiaiko.auth_service.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.auth_service.service.RefreshTokenService;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RefreshControllerTest {
    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RefreshController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String json(String token) throws Exception {
        return objectMapper.writeValueAsString(new RefreshRequest(token));
    }

    @Test
    public void refresh_valid_returnsNewPair() throws Exception {
        when(refreshTokenService.validateAndRotate("r")).thenReturn("John.Doe");
        when(tokenService.generateToken("John.Doe")).thenReturn("new.access");
        when(refreshTokenService.issue("John.Doe")).thenReturn("new.refresh");

        mockMvc.perform(post("/oauth2/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("r")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh"));
    }

    @Test
    public void refresh_invalid_401() throws Exception {
        when(refreshTokenService.validateAndRotate(any())).thenThrow(InvalidRefreshTokenException.class);

        mockMvc.perform(post("/oauth2/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("r")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void refresh_blankToken_400() throws Exception {
        mockMvc.perform(post("/oauth2/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("")))
                .andExpect(status().isBadRequest());
    }
}
