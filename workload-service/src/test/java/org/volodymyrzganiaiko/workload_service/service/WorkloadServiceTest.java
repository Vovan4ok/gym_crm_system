package org.volodymyrzganiaiko.workload_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import org.volodymyrzganiaiko.workload_service.dto.ActionType;
import org.volodymyrzganiaiko.workload_service.dto.TrainerSummaryResponse;
import org.volodymyrzganiaiko.workload_service.dto.TrainerWorkloadRequest;
import org.volodymyrzganiaiko.workload_service.repository.TrainerWorkloadRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkloadServiceTest {
    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private WorkloadService workloadService;

    private TrainerWorkloadRequest req(int minutes, ActionType action) {
        return new TrainerWorkloadRequest("Tra.Iner", "Tra", "Iner", true,
                LocalDate.of(2026, 7, 10), minutes, action);
    }

    private TrainerWorkload existing(int minutes) {
        return new TrainerWorkload("Tra.Iner", "Tra", "Iner", true,
                new ArrayList<>(List.of(new TrainerWorkload.YearSummary(2026,
                        new ArrayList<>(List.of(new TrainerWorkload.MonthSummary(Month.JULY, minutes)))))));
    }

    @Test
    public void add_noDocument_creates() {
        when(repository.findByUsername("Tra.Iner")).thenReturn(Optional.empty());

        workloadService.process(req(60, ActionType.ADD));

        verify(repository).save(argThat(w ->
                w.getUsername().equals("Tra.Iner")
                && w.getYears().get(0).getYear() == 2026
                && w.getYears().get(0).getMonths().get(0).getMonth() == Month.JULY
                && w.getYears().get(0).getMonths().get(0).getSummaryDuration() == 60));
    }

    @Test
    void add_existing_accumulates() {
        when(repository.findByUsername("Tra.Iner")).thenReturn(Optional.of(existing(60)));

        workloadService.process(req(30, ActionType.ADD));

        verify(repository).save(argThat(w ->
                w.getYears().get(0).getMonths().get(0).getSummaryDuration() == 90));
    }

    @Test
    void delete_toZero_removesMonthAndYear() {
        when(repository.findByUsername("Tra.Iner")).thenReturn(Optional.of(existing(30)));

        workloadService.process(req(30, ActionType.DELETE));

        verify(repository).save(argThat(w -> w.getYears().isEmpty()));
    }

    @Test
    void delete_unknown_noSave() {
        when(repository.findByUsername("Tra.Iner")).thenReturn(Optional.empty());

        workloadService.process(req(30, ActionType.DELETE));

        verify(repository, never()).save(any());
    }

    @Test
    void getWorkload_found_maps() {
        when(repository.findByUsername("Tra.Iner")).thenReturn(Optional.of(existing(60)));
        TrainerSummaryResponse r = workloadService.getWorkload("Tra.Iner");
        assertEquals("Tra.Iner", r.username());
        assertTrue(r.active());
        assertEquals(2026, r.years().get(0).year());
        assertEquals(Month.JULY, r.years().get(0).months().get(0).month());
        assertEquals(60, r.years().get(0).months().get(0).summaryDuration());
    }

    @Test
    void getWorkload_notFound_throws() {
        when(repository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> workloadService.getWorkload("x"));
    }
}
