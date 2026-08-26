package org.volodymyrzganiaiko.auth_service.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.volodymyrzganiaiko.auth_service.dto.RefreshRequest;
import org.volodymyrzganiaiko.auth_service.dto.RefreshResponse;
import org.volodymyrzganiaiko.auth_service.service.RefreshTokenService;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

@RestController
@RequestMapping("/oauth2/refresh")
public class RefreshController {
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;

    public RefreshController(RefreshTokenService refreshTokenService, TokenService tokenService) {
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        String username = refreshTokenService.validateAndRotate(refreshRequest.refreshToken());
        String accessToken = tokenService.generateToken(username);
        String refreshToken = refreshTokenService.issue(username);
        return new RefreshResponse(accessToken, refreshToken);
    }
}
