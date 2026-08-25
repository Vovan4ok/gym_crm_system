package org.volodymyrzganiaiko.auth_service.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.volodymyrzganiaiko.auth_service.client.GymAuthClient;
import org.volodymyrzganiaiko.auth_service.dto.LoginRequest;
import org.volodymyrzganiaiko.auth_service.dto.LoginResponse;
import org.volodymyrzganiaiko.auth_service.exception.UserBlockedException;
import org.volodymyrzganiaiko.auth_service.service.BruteForceProtectionService;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

@RestController
@RequestMapping("/api/login")
public class AuthController {
    private final GymAuthClient gymAuthClient;
    private final TokenService tokenService;
    private final BruteForceProtectionService bruteForceProtectionService;

    public AuthController(GymAuthClient gymAuthClient, TokenService tokenService, BruteForceProtectionService bruteForceProtectionService) {
        this.gymAuthClient = gymAuthClient;
        this.tokenService = tokenService;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> auth(@Valid @RequestBody LoginRequest loginRequest) {
        if (bruteForceProtectionService.isBlocked(loginRequest.username())) {
            throw new UserBlockedException("Too many failed login attempts");
        }
        try {
            gymAuthClient.verify(loginRequest);
        } catch (FeignException.Unauthorized e) {
            bruteForceProtectionService.loginFailed(loginRequest.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = tokenService.generateToken(loginRequest.username());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
