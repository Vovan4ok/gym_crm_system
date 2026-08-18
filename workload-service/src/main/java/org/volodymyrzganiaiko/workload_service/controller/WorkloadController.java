package org.volodymyrzganiaiko.workload_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.service.WorkloadService;

@RestController
@RequestMapping("/api/workload")
public class WorkloadController {
    private final WorkloadService workloadService;

    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    public ResponseEntity<Void> submitData(@Valid @RequestBody TrainerWorkloadRequest request) {
        workloadService.process(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerSummaryResponse> getWorkloadSummary(@PathVariable String username) {
        return ResponseEntity.ok(workloadService.getWorkload(username));
    }
}
