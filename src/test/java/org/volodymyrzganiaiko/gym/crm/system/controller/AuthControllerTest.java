package org.volodymyrzganiaiko.gym.crm.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.volodymyrzganiaiko.gym.crm.system.dto.ChangePasswordRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.LoginRequest;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;
import org.volodymyrzganiaiko.gym.crm.system.handler.GlobalExceptionHandler;
import org.volodymyrzganiaiko.gym.crm.system.service.BruteForceProtectionService;
import org.volodymyrzganiaiko.gym.crm.system.service.JwtService;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private GymFacade gymFacade;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;
    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

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
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void login_post() throws Exception {
        LoginRequest input = new LoginRequest("John.Doe", "rawPass");
        String json = objectMapper.writeValueAsString(input);

        when(jwtService.generateToken("John.Doe")).thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    public void login_invalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest input = new LoginRequest("John.Doe", "rawPass");
        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Wrong username or password"))
                .andExpect(content().string(not(containsString("rawPass"))));

        verify(bruteForceProtectionService).loginFailed("John.Doe");
    }

    @Test
    public void login_blocked() throws Exception {
        when(bruteForceProtectionService.isBlocked("John.Doe")).thenReturn(true);

        LoginRequest input = new LoginRequest("John.Doe", "rawPass");
        String json = objectMapper.writeValueAsString(input);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Too many login failed attempts"))
                .andExpect(content().string(not(containsString("rawPass"))));

        verifyNoInteractions(authenticationManager);
    }
    @Test
    public void changePassword_success() throws Exception {
        authenticateAs("John.Doe");

        ChangePasswordRequest request = new ChangePasswordRequest("newPass");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
        .andExpect(status().isOk());

        verify(gymFacade).changeLogin("John.Doe", "newPass");
    }

    @Test
    public void changePassword_emptyPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("newPassword")));

        verifyNoInteractions(gymFacade);
    }

    private void authenticateAs(String username) {
        UserDetails principal = User.withUsername(username).password("x").authorities("ROLE_USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
