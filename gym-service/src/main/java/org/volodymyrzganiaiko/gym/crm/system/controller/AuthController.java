package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.ChangePasswordRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.LoginRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.LoginResponse;
import org.volodymyrzganiaiko.gym.crm.system.security.exception.UserBlockedException;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;
import org.volodymyrzganiaiko.gym.crm.system.security.service.BruteForceProtectionService;
import org.volodymyrzganiaiko.gym.crm.system.security.service.JwtService;

@RestController
@RequestMapping("/api/login")
@Tag(name = "User authorization")
public class AuthController {
    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BruteForceProtectionService bruteForceProtectionService;

    @PostMapping
    @Operation(summary = "Log in", description = "Verifies the credentials passed in the body.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        if (bruteForceProtectionService.isBlocked(req.username())) {
            throw new UserBlockedException("Too many failed login attempts");
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        } catch (AuthenticationException e) {
            bruteForceProtectionService.loginFailed(req.username());
            throw e;
        }
        bruteForceProtectionService.loginSucceeded(req.username());
        String token = jwtService.generateToken(req.username());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PutMapping
    @Operation(summary = "Change password", description = "Replaces the password of the authenticated user with the one supplied in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The new password is missing or does not satisfy the constraints"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password")
    })
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        gymFacade.changeLogin(userDetails.getUsername(), changePasswordRequest.newPassword());
        return ResponseEntity.ok().build();
    }
}
