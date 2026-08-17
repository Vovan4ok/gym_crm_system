package org.volodymyrzganiaiko.gym.crm.system.security.handler;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.volodymyrzganiaiko.gym.crm.system.security.service.TokenDenylistService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenLogoutHandlerTest {
    @Mock
    private TokenDenylistService tokenDenylistService;

    @InjectMocks
    private TokenLogoutHandler tokenLogoutHandler;

    @Test
    public void validToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenLogoutHandler.logout(request, response, null);

        verify(tokenDenylistService).deny("token");
    }

    @Test
    public void trashToken() {
        doThrow(new JwtException("bad")).when(tokenDenylistService).deny("bad");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad");
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenLogoutHandler.logout(request, response, null);
    }

    @Test
    public void withoutHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        tokenLogoutHandler.logout(request, response, null);

        verifyNoInteractions(tokenDenylistService);
    }
}
