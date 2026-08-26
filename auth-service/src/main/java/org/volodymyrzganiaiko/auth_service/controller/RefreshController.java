package org.volodymyrzganiaiko.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Token refresh")
public class RefreshController {
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;

    public RefreshController(RefreshTokenService refreshTokenService, TokenService tokenService) {
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
    }

    @PostMapping
    @Operation(summary = "Refresh tokens", description = "Exchanges a valid refresh token for a new access token and a new refresh token; the presented refresh token is rotated out.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A new access and refresh token pair was issued"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "The refresh token is invalid or expired")
    })
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        String username = refreshTokenService.validateAndRotate(refreshRequest.refreshToken());
        String accessToken = tokenService.generateToken(username);
        String refreshToken = refreshTokenService.issue(username);
        return new RefreshResponse(accessToken, refreshToken);
    }
}
