package org.volodymyrzganiaiko.workload_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.workload_service.dto.ActionType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadServiceTest {
    private WorkloadService workloadService;

    @BeforeEach
    public void setUp() {
        this.workloadService = new WorkloadService();
    }

    @Test
    public void process_add_accumulates() {
        for (int i = 0; i < 2; i++) {
            workloadService.process(new TrainerWorkloadRequest(
                    "Tra.Iner",
                    "Tra",
                    "Iner",
                    true,
                    LocalDate.parse("2026-08-19"),
                    60,
                    ActionType.ADD
            ));
        }

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertEquals(2026, result.years().get(0).year());
        assertEquals(1, result.years().get(0).months().size());
        assertEquals(120, result.years().get(0).months().get(0).summaryDuration());
    }

    @Test
    public void process_add_differentMonths() {
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-09-19"),
                60,
                ActionType.ADD
        ));

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertEquals(2, result.years().get(0).months().size());
    }

    @Test
    public void process_delete_subtracts() {
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                20,
                ActionType.DELETE
        ));

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertEquals(40, result.years().get(0).months().get(0).summaryDuration());
    }

    @Test
    public void process_delete_overRemovesMonth() {
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertEquals(1, result.years().get(0).months().size());
        assertEquals(60, result.years().get(0).months().get(0).summaryDuration());

        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                80,
                ActionType.DELETE
        ));

        result = workloadService.getWorkload("Tra.Iner");

        assertEquals(0, result.years().get(0).months().size());
    }

    @Test
    public void process_delete_missingTrainer_noop() {
        assertDoesNotThrow(() -> workloadService.process(
                new TrainerWorkloadRequest(
                        "Tra.Iner",
                        "Tra",
                        "Iner",
                        true,
                        LocalDate.parse("2026-08-19"),
                        80,
                        ActionType.DELETE
                )
        ));
    }

    @Test
    public void process_add_updatesIdentity() {
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertTrue(result.active());

        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                false,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));

        result = workloadService.getWorkload("Tra.Iner");

        assertFalse(result.active());
    }

    @Test
    public void getWorkload_returnsNestedSummary() {
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2026-08-19"),
                60,
                ActionType.ADD
        ));
        workloadService.process(new TrainerWorkloadRequest(
                "Tra.Iner",
                "Tra",
                "Iner",
                true,
                LocalDate.parse("2027-09-19"),
                60,
                ActionType.ADD
        ));

        TrainerSummaryResponse result = workloadService.getWorkload("Tra.Iner");

        assertEquals(2, result.years().size());
        assertEquals(1, result.years().get(0).months().size());
        assertEquals(1, result.years().get(1).months().size());
    }

    @Test
    public void getWorkload_notFound() {
        assertThrows(NoSuchElementException.class, () -> workloadService.getWorkload("Ghost"));
    }
}
