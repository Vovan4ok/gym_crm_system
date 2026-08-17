package org.volodymyrzganiaiko.gym.crm.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainer;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;
import org.volodymyrzganiaiko.gym.crm.system.security.service.JwtService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers")
public class TrainerController {
    @Autowired
    private GymFacade gymFacade;
    @Autowired
    private JwtService jwtService;

    @PostMapping
    @Operation(summary = "Register a trainer", description = "Creates a trainer and returns the generated username, the generated password and a JWT bearer token for the new user. No authentication is required.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer was successfully created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "404", description = "The specialization was not found")
    })
    public ResponseEntity<RegistrationResponse> createTrainer(@Valid @RequestBody TrainerRegistrationRequest req) {
        Trainer trainer = mapTrainer(req.firstName(), req.lastName(), null, new TrainingType(req.specializationId(), null));
        TrainerRegistrationDTO dto = gymFacade.createTrainer(trainer);
        String token = jwtService.generateToken(dto.username());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(dto.username(), dto.password(), token));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get a trainer profile", description = "Returns the profile of the trainer identified by the path variable, including the list of assigned trainees.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainer was not found")
    })
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(gymFacade.getTrainerProfile(username));
    }

    @PutMapping("/{username}")
    @Operation(summary = "Update a trainer profile", description = "Overwrites the editable fields of the trainer and returns the updated profile. The specialization cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainer was not found")
    })
    public ResponseEntity<TrainerProfileResponse> updateProfile(@PathVariable String username, @Valid @RequestBody UpdateTrainerRequest req) {
        Trainer trainer = mapTrainer(req.firstName(), req.lastName(), req.isActive(), null);
        return ResponseEntity.ok(gymFacade.updateTrainerProfile(username, trainer));
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainer", description = "Switches the active flag of the trainer. The operation is not idempotent: setting the flag to the value it already has is rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainer was not found"),
            @ApiResponse(responseCode = "409", description = "The trainer is already in the requested state")
    })
    public ResponseEntity<Void> changeStatus(@PathVariable String username, @Valid @RequestBody UpdateStatusRequest req) {
        gymFacade.changeTrainerStatus(username, req.isActive());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "List the trainings of a trainer", description = "Returns the trainings of the trainer. Every filter is optional; omitting all of them returns the full list.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "A date filter is not in the yyyy-MM-dd format"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainer was not found")
    })
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(@PathVariable String username, @Parameter(description = "Lower bound of the training date, inclusive", example = "2026-01-31") @RequestParam(required = false, value = "periodFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @Parameter(description = "Upper bound of the training date, inclusive", example = "2026-12-31") @RequestParam(required = false, value = "periodTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @Parameter(description = "Filter by the trainee's first name") @RequestParam(required = false) String traineeName) {
        return ResponseEntity.ok(gymFacade.getTrainerTrainings(username, from, to, traineeName));
    }

    private Trainer mapTrainer(String firstName, String lastName, Boolean isActive, TrainingType specialization) {
        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setIsActive(isActive);
        trainer.setSpecialization(specialization);
        return trainer;
    }
}
