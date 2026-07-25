package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short trainee reference used inside a trainer profile")
public record TraineeSummaryResponse(
        @Schema(description = "Generated username of the trainee", example = "Tr.Ainee")
        String username,

        @Schema(description = "First name of the trainee", example = "John")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        String lastName) {
}