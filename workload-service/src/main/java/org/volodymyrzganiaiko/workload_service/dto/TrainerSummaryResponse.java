package org.volodymyrzganiaiko.workload_service.dto;

import java.util.List;

public record TrainerSummaryResponse(String username, String firstName, String lastName, boolean active, List<YearlySummaryResponse> years) {
}
