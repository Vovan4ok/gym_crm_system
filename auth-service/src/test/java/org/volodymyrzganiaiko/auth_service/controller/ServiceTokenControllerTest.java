package org.volodymyrzganiaiko.auth_service.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.volodymyrzganiaiko.auth_service.dto.ServiceTokenRequest;
import org.volodymyrzganiaiko.auth_service.dto.ServiceTokenResponse;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServiceTokenControllerTest {
    @Test
    public void correctCredentials_returnsToken() {
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.generateToken("gym-service")).thenReturn("test.token");
        ServiceTokenController controller = new ServiceTokenController(tokenService, "gym-service", "secret", 3600);

        ResponseEntity<ServiceTokenResponse> resp = controller.getToken(new ServiceTokenRequest("gym-service", "secret"));

        assertEquals(200, resp.getStatusCode().value());
        Assertions.assertNotNull(resp.getBody());
        assertEquals("test.token", resp.getBody().token());
        assertEquals(3600, resp.getBody().expiresInSeconds());
    }

    @Test
    public void wrongSecret_401() {
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.generateToken("gym-service")).thenReturn("test.token");
        ServiceTokenController controller = new ServiceTokenController(tokenService, "gym-service", "secret", 3600);

        ResponseEntity<ServiceTokenResponse> resp = controller.getToken(new ServiceTokenRequest("gym-service", "wrong"));

        assertEquals(401, resp.getStatusCode().value());
        verifyNoInteractions(tokenService);
    }
    
    @Test
    public void wrongId_401() {
        TokenService tokenService = mock(TokenService.class);
        when(tokenService.generateToken("gym-service")).thenReturn("test.token");
        ServiceTokenController controller = new ServiceTokenController(tokenService, "gym-service", "secret", 3600);

        ResponseEntity<ServiceTokenResponse> resp = controller.getToken(new ServiceTokenRequest("wrong", "secret"));

        assertEquals(401, resp.getStatusCode().value());
        verifyNoInteractions(tokenService);
    }
}
