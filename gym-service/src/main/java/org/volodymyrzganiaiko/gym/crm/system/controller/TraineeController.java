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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.dto.*;
import org.volodymyrzganiaiko.gym.crm.system.facade.GymFacade;

import jakarta.validation.Valid;
import org.volodymyrzganiaiko.gym.crm.system.security.service.JwtService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees")
public class TraineeController {
    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private JwtService jwtService;

    @PostMapping
    @Operation(summary = "Register a trainee", description = "Creates a trainee and returns the generated username, the generated password and a JWT bearer token for the new user. No authentication is required.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee was successfully created"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation")
    })
    public ResponseEntity<RegistrationResponse> createTrainee(@Valid @RequestBody TraineeRegistrationRequest  req) {
        Trainee trainee = mapTrainee(req.firstName(), req.lastName(), null, req.dateOfBirth(), req.address());
        TraineeRegistrationDTO dto = gymFacade.createTrainee(trainee);
        String token = jwtService.generateToken(dto.username());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(dto.username(), dto.password(), token));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get a trainee profile", description = "Returns the profile of the trainee identified by the path variable, including the list of assigned trainers.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found")
    })
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(gymFacade.getTraineeProfile(username));
    }

    @PreAuthorize("#username == authentication.name")
    @PutMapping("/{username}")
    @Operation(summary = "Update a trainee profile", description = "Overwrites the editable fields of the trainee and returns the updated profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found")
    })
    public ResponseEntity<TraineeProfileResponse> updateProfile(@PathVariable String username, @Valid @RequestBody UpdateTraineeRequest req) {
        Trainee trainee = mapTrainee(req.firstName(), req.lastName(), req.isActive(), req.dateOfBirth(), req.address());
        return ResponseEntity.ok(gymFacade.updateTraineeProfile(username, trainee));
    }

    @PreAuthorize("#username == authentication.name")
    @DeleteMapping("/{username}")
    @Operation(summary = "Delete a trainee profile", description = "Removes the trainee together with all of their trainings.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found")
    })
    public ResponseEntity<Void> deleteProfile(@PathVariable String username) {
        gymFacade.deleteTraineeProfile(username);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "List trainers not assigned to the trainee", description = "Returns the active trainers that are not yet linked with this trainee.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found")
    })
    public ResponseEntity<List<TrainerSummaryResponse>> getUnassignedTrainers(@PathVariable String username) {
        return ResponseEntity.ok(gymFacade.getUnassignedTrainers(username));
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Replace the trainer list of a trainee", description = "Replaces the whole set of trainers assigned to the trainee and returns the resulting list.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee or one of the trainers was not found")
    })
    public ResponseEntity<List<TrainerSummaryResponse>> updateTrainers(@PathVariable String username, @Valid @RequestBody UpdateTrainerListRequest req) {
        return ResponseEntity.ok(gymFacade.updateTrainers(username, req.usernames()));
    }

    @PreAuthorize("#username == authentication.name")
    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a trainee", description = "Switches the active flag of the trainee. The operation is not idempotent: setting the flag to the value it already has is rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found"),
            @ApiResponse(responseCode = "409", description = "The trainee is already in the requested state")
    })
    public ResponseEntity<Void> changeStatus(@PathVariable String username, @Valid @RequestBody UpdateStatusRequest req) {
        gymFacade.changeTraineeStatus(username, req.isActive());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/{username}/trainings")
    @Operation(summary = "List the trainings of a trainee", description = "Returns the trainings of the trainee. Every filter is optional; omitting all of them returns the full list.")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "A date filter is not in the yyyy-MM-dd format"),
            @ApiResponse(responseCode = "401", description = "Wrong username or password"),
            @ApiResponse(responseCode = "404", description = "The trainee was not found")
    })
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(@PathVariable String username, @Parameter(description = "Lower bound of the training date, inclusive", example = "2026-01-31") @RequestParam(required = false, value = "periodFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @Parameter(description = "Upper bound of the training date, inclusive", example = "2026-12-31") @RequestParam(required = false, value = "periodTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @Parameter(description = "Filter by the trainer's first name") @RequestParam(required = false) String trainerName, @Parameter(description = "Filter by the training type name") @RequestParam(required = false) String trainingType) {
        return ResponseEntity.ok(gymFacade.getTraineeTrainings(username, from, to, trainerName, trainingType));
    }

    private Trainee mapTrainee(String firstName, String lastName, Boolean isActive, LocalDate dateOfBirth, String address) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setIsActive(isActive);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        return trainee;
    }
}
