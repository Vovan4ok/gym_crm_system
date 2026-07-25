package org.volodymyrzganiaiko.gym.crm.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training type available in the system")
public record TrainingTypeResponse(
        @Schema(description = "Identifier of the training type", example = "2")
        Long id,

        @Schema(description = "Display name of the training type", example = "Cardio")
        String name) {
}
