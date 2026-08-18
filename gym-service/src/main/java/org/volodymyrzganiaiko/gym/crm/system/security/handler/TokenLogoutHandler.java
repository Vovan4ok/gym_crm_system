package org.volodymyrzganiaiko.gym.crm.system.security.handler;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.security.service.TokenDenylistService;

@Component
public class TokenLogoutHandler implements LogoutHandler {
    private final TokenDenylistService tokenDenylistService;

    public TokenLogoutHandler(TokenDenylistService tokenDenylistService) {
        this.tokenDenylistService = tokenDenylistService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                tokenDenylistService.deny(token);
            } catch (JwtException e) {
                // invalid token on logout — nothing to deny
            }
        }
    }
}
