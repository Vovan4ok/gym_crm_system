package org.volodymyrzganiaiko.workload_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload.*;
import org.volodymyrzganiaiko.workload_service.dto.*;
import org.volodymyrzganiaiko.workload_service.repository.TrainerWorkloadRepository;

import java.time.Month;
import java.util.*;

@Slf4j
@Service
public class WorkloadService {
    private final TrainerWorkloadRepository repository;

    public WorkloadService(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    public void process(TrainerWorkloadRequest request) {
        int year = request.trainingDate().getYear();
        Month month = request.trainingDate().getMonth();
        log.info("Processing workload event: trainer={}, action={}, minutes={}",
                request.trainerUsername(), request.actionType(), request.trainingDuration());

        TrainerWorkload workload = repository.findByUsername(request.trainerUsername()).orElse(null);

        if (request.actionType() == ActionType.ADD) {
            if (workload == null) {
                log.debug("No document for {}, creating a new one", request.trainerUsername());
                workload = new TrainerWorkload(request.trainerUsername(), null, null, null, new ArrayList<>());
            }
            workload.setFirstName(request.firstName());
            workload.setLastName(request.lastName());
            workload.setActive(request.isActive());
            MonthSummary monthSummary = findOrCreateMonth(workload, year, month);
            monthSummary.setSummaryDuration(monthSummary.getSummaryDuration() + request.trainingDuration());
            log.debug("Added {} min to {}/{} for {}", request.trainingDuration(), year, month, request.trainerUsername());
            repository.save(workload);
        } else {
            if (workload == null) {
                log.warn("DELETE for unknown trainer {}, ignoring", request.trainerUsername());
                return;
            }
            subtract(workload, year, month, request.trainingDuration());
            repository.save(workload);
            log.debug("Subtracted {} min from {}/{} for {}", request.trainingDuration(), year, month, request.trainerUsername());
        }
    }

    public TrainerSummaryResponse getWorkload(String username) {
        log.debug("Fetching workload for {}", username);
        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException(
                        "The trainer with username " + username + " does not have any workload"));
        return new TrainerSummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                Boolean.TRUE.equals(workload.getActive()),
                workload.getYears().stream()
                        .map(y -> new YearlySummaryResponse(y.getYear(),
                                y.getMonths().stream()
                                        .map(m -> new MonthlySummaryResponse(m.getMonth(), m.getSummaryDuration()))
                                        .toList()))
                        .toList());
    }

    private MonthSummary findOrCreateMonth(TrainerWorkload workload, int year, Month month) {
        YearSummary yearSummary = workload.getYears().stream()
                .filter(y -> y.getYear() == year).findFirst()
                .orElseGet(() -> {
                    YearSummary y = new YearSummary(year, new ArrayList<>());
                    workload.getYears().add(y);
                    return y;
                });
        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month).findFirst()
                .orElseGet(() -> {
                    MonthSummary m = new MonthSummary(month, 0);
                    yearSummary.getMonths().add(m);
                    return m;
                });
    }

    private void subtract(TrainerWorkload workload, int year, Month month, int minutes) {
        for (Iterator<YearSummary> yIt = workload.getYears().iterator(); yIt.hasNext();) {
            YearSummary y = yIt.next();
            if (y.getYear() != year) continue;
            for (Iterator<MonthSummary> mIt = y.getMonths().iterator(); mIt.hasNext();) {
                MonthSummary m = mIt.next();
                if (m.getMonth() != month) continue;
                m.setSummaryDuration(m.getSummaryDuration() - minutes);
                if (m.getSummaryDuration() <= 0) mIt.remove();
                break;
            }
            if (y.getMonths().isEmpty()) yIt.remove();
            break;
        }
    }
}
