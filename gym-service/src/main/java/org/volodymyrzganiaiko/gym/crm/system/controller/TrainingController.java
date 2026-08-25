package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.dto.AddTrainingRequest;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Trainings")
public class TrainingController {
    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(summary = "Add a training", description = "Creates a training and links the trainee with the trainer if they were not linked yet. The training type is taken from the trainer's specialization.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Training was successfully created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee or the trainer was not found")
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest req) {
        gymFacade.createTraining(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a training", description = "Deletes the training identified by the id and notifies the workload service to subtract the trainer's hours for that session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training was successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The training was not found")
    })
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        gymFacade.deleteTraining(id);
        return ResponseEntity.ok().build();
    }
}
