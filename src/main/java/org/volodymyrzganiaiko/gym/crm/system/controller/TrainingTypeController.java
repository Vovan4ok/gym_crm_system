package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.volodymyrzganiaiko.gym.crm.system.dto.TrainingTypeResponse;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training types")
public class TrainingTypeController {
    @Autowired
    private GymFacade gymFacade;

    @GetMapping
    @Operation(summary = "List training types", description = "Returns every training type available in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password")
    })
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(gymFacade.getTrainingTypes());
    }
}
