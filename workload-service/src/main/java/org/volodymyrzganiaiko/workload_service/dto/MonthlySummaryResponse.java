package org.volodymyrzganiaiko.workload_service.dto;

import java.time.Month;

public record MonthlySummaryResponse(Month month, int summaryDuration) {
}
