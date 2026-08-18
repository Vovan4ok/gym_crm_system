package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Replacement set of trainers for a trainee. The list is applied as a whole, not merged with the current one.")
public record UpdateTrainerListRequest(
        @Schema(description = "Usernames of the trainers to assign. An empty list removes every assignment.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull List<String> usernames) {
}