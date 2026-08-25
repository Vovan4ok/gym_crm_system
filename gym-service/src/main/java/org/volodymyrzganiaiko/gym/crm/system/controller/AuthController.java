package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.ChangePasswordRequest;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/login")
@Tag(name = "User authorization")
public class AuthController {
    private final GymFacade gymFacade;

    public AuthController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PutMapping
    @Operation(summary = "Change password", description = "Replaces the password of the authenticated user with the one supplied in the request body.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The new password is missing or does not satisfy the constraints"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password")
    })
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        gymFacade.changeLogin(jwt.getSubject(), changePasswordRequest.newPassword());
        return ResponseEntity.ok().build();
    }
}
