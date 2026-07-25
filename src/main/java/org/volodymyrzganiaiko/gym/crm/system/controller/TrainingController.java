package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.AddTrainingRequest;
import org.volodymyrzganiaiko.gym.crm.system.dto.Credentials;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings")
public class TrainingController {
    @Autowired
    private GymFacade gymFacade;

    @PostMapping
    @Operation(summary = "Add a training", description = "Creates a training and links the trainee with the trainer if they were not linked yet. The training type is taken from the trainer's specialization.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Training was successfully created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee or the trainer was not found")
    })
    public ResponseEntity<Void> addTraining(Credentials credentials, @Valid @RequestBody AddTrainingRequest req) {
        gymFacade.createTraining(credentials, req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
