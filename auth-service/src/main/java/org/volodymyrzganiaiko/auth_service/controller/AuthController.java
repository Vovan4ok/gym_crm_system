package org.volodymyrzganiaiko.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.volodymyrzganiaiko.auth_service.dto.LoginRequest;
import org.volodymyrzganiaiko.auth_service.dto.LoginResponse;
import org.volodymyrzganiaiko.auth_service.exception.UserBlockedException;
import org.volodymyrzganiaiko.auth_service.security.service.CredentialVerificationService;
import org.volodymyrzganiaiko.auth_service.service.BruteForceProtectionService;
import org.volodymyrzganiaiko.auth_service.service.RefreshTokenService;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

@RestController
@RequestMapping("/api/login")
@Tag(name = "User authentication")
public class AuthController {
    private final TokenService tokenService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final RefreshTokenService refreshTokenService;
    private final CredentialVerificationService credentialVerificationService;

    public AuthController(TokenService tokenService, BruteForceProtectionService bruteForceProtectionService, RefreshTokenService refreshTokenService, CredentialVerificationService credentialVerificationService) {
        this.tokenService = tokenService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.refreshTokenService = refreshTokenService;
        this.credentialVerificationService = credentialVerificationService;
    }

    @PostMapping
    @Operation(summary = "Authenticate a user", description = "Verifies the credentials against gym-service and returns a signed access token together with an opaque refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded; access and refresh tokens returned"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "429", description = "Too many failed login attempts; the user is temporarily blocked")
    })
    public ResponseEntity<LoginResponse> auth(@Valid @RequestBody LoginRequest loginRequest) {
        if (bruteForceProtectionService.isBlocked(loginRequest.username())) {
            throw new UserBlockedException("Too many failed login attempts");
        }
        try {
            credentialVerificationService.verify(loginRequest.username(), loginRequest.password());
        } catch (BadCredentialsException e) {
            bruteForceProtectionService.loginFailed(loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        bruteForceProtectionService.loginSucceeded(loginRequest.username());
        String accessToken = tokenService.generateToken(loginRequest.username());
        String refreshToken = refreshTokenService.issue(loginRequest.username());
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
    }
}
