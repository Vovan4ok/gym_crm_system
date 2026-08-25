package org.volodymyrzganiaiko.auth_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.volodymyrzganiaiko.auth_service.dto.ServiceTokenRequest;
import org.volodymyrzganiaiko.auth_service.dto.ServiceTokenResponse;
import org.volodymyrzganiaiko.auth_service.service.TokenService;

import java.security.MessageDigest;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class ServiceTokenController {
    private final TokenService tokenService;
    private final String clientId;
    private final String clientSecret;
    private final long expirationInSeconds;

    public ServiceTokenController(TokenService tokenService, @Value("${security.service-client.id}") String clientId, @Value("${security.service-client.secret}") String clientSecret, @Value("${jwt.expiration-seconds}") long expirationInSeconds) {
        this.tokenService = tokenService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.expirationInSeconds = expirationInSeconds;
    }

    @PostMapping("/oauth2/token")
    public ResponseEntity<ServiceTokenResponse> getToken(@Valid @RequestBody ServiceTokenRequest tokenRequest) {
        boolean idOk = MessageDigest.isEqual(clientId.getBytes(UTF_8), tokenRequest.clientId().getBytes(UTF_8));
        boolean secretOk = MessageDigest.isEqual(clientSecret.getBytes(UTF_8), tokenRequest.clientSecret().getBytes(UTF_8));
        if (!idOk || !secretOk) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = tokenService.generateToken(tokenRequest.clientId());
        return ResponseEntity.ok(new ServiceTokenResponse(token, expirationInSeconds));
    }
}
