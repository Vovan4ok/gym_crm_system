package org.volodymyrzganiaiko.gym.crm.system.security.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenDenylistServiceTest {
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenDenylistService service;

    @Test
    public void deny_deniedToken() {
        when(jwtService.extractExpiration("token")).thenReturn(5L);

        service.deny("token");

        assertTrue(service.isDenied("token"));
        assertFalse(service.isDenied("diff_token"));
    }

    @Test
    public void purge_dropsExpiredToken() {
        when(jwtService.extractExpiration("token")).thenReturn(System.currentTimeMillis() - 1000L);

        service.deny("token");
        service.purgeExpired();

        assertFalse(service.isDenied("token"));
    }

    @Test
    public void purge_savesLiveToken() {
        when(jwtService.extractExpiration("token")).thenReturn(System.currentTimeMillis() + 100000L);

        service.deny("token");
        service.purgeExpired();

        assertTrue(service.isDenied("token"));
    }
}
