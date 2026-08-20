package org.volodymyrzganiaiko.workload_service.dto;

import java.util.List;

public record YearlySummaryResponse(int year, List<MonthlySummaryResponse> months) {
}
