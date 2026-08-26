package org.volodymyrzganiaiko.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Service tokens")
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
    @Operation(summary = "Issue a service token", description = "Issues a signed access token for a trusted service using the client-credentials flow.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A service access token was issued"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Unknown client id or wrong client secret")
    })
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
