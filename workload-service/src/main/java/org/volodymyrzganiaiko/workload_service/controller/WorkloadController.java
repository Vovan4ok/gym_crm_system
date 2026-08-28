package org.volodymyrzganiaiko.workload_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

@RestController
@RequestMapping("/api/workload")
@Tag(name = "Trainer workload")
public class WorkloadController {
    private final WorkloadService workloadService;

    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get a trainer's workload summary",
            description = "Returns the nested year → month → duration summary for the trainer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "The trainer was not found")
    })
    public ResponseEntity<TrainerSummaryResponse> getWorkloadSummary(@PathVariable String username) {
        return ResponseEntity.ok(workloadService.getWorkload(username));
    }
}
