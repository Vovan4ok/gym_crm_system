package org.volodymyrzganiaiko.workload_service.rpc;

import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.workload_service.dto.MonthlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.YearlySummaryResponse;
import org.volodymyrzganiaiko.workload_service.grpc.CalendarMonth;
import org.volodymyrzganiaiko.workload_service.grpc.GetWorkloadResponse;

import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadProtoMapperTest {
    private final WorkloadProtoMapper mapper = new WorkloadProtoMapper();

    @Test
    public void toProto_mapsNestedYearsAndMonths() {
        TrainerSummaryResponse summary = new TrainerSummaryResponse("Tra.Iner", "Tra", "Iner", false, List.of(
                new YearlySummaryResponse(2025, List.of(new MonthlySummaryResponse(Month.DECEMBER, 30))),
                new YearlySummaryResponse(2026, List.of(
                        new MonthlySummaryResponse(Month.JANUARY, 60),
                        new MonthlySummaryResponse(Month.MARCH, 45)))));

        GetWorkloadResponse proto = mapper.toProto(summary);

        assertEquals("Tra.Iner", proto.getTrainerUsername());
        assertEquals("Iner", proto.getLastName());
        assertFalse(proto.getActive());
        assertEquals(2, proto.getYearsCount());
        assertEquals(CalendarMonth.MONTH_DECEMBER, proto.getYears(0).getMonths(0).getMonth());
        assertEquals(2, proto.getYears(1).getMonthsCount());
        assertEquals(CalendarMonth.MONTH_MARCH, proto.getYears(1).getMonths(1).getMonth());
        assertEquals(45, proto.getYears(1).getMonths(1).getSummaryDuration());
    }

    @Test
    public void toProto_nullNames_becomeEmptyStrings() {
        TrainerSummaryResponse summary = new TrainerSummaryResponse("Tra.Iner", null, null, true, List.of());

        GetWorkloadResponse proto = mapper.toProto(summary);

        assertEquals("", proto.getFirstName());
        assertEquals("", proto.getLastName());
        assertEquals(0, proto.getYearsCount());
    }

    @Test
    public void toProto_everyJavaMonth_mapsToTheSameNumberedEnumValue() {
        Arrays.stream(Month.values()).forEach(month -> {
            TrainerSummaryResponse summary = new TrainerSummaryResponse("u", "f", "l", true,
                    List.of(new YearlySummaryResponse(2026, List.of(new MonthlySummaryResponse(month, 1)))));

            CalendarMonth mapped = mapper.toProto(summary).getYears(0).getMonths(0).getMonth();

            assertEquals(month.getValue(), mapped.getNumber());
            assertTrue(mapped.name().endsWith(month.name()));
        });
    }
}
