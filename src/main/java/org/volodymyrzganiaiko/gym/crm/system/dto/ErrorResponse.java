package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Body returned for every failed request")
public record ErrorResponse (
        @Schema(description = "HTTP status code, repeated in the body for convenience", example = "404")
        int status,

        @Schema(description = "Human readable reason for the failure", example = "Trainee with the username Tr.Ainee was not found")
        String message,

        @Schema(description = "Server time the error was produced at", example = "2026-07-20T10:15:30")
        LocalDateTime timestamp) {
}