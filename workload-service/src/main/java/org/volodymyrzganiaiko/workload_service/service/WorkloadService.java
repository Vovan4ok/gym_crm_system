package org.volodymyrzganiaiko.workload_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload.*;
import org.volodymyrzganiaiko.workload_service.dto.*;
import org.volodymyrzganiaiko.workload_service.repository.TrainerWorkloadRepository;
import reactor.core.publisher.Mono;

import java.time.Month;
import java.util.*;

@Slf4j
@Service
public class WorkloadService {
    private final TrainerWorkloadRepository repository;

    public WorkloadService(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    public Mono<Void> process(TrainerWorkloadRequest req) {
        int year = req.trainingDate().getYear();
        Month month = req.trainingDate().getMonth();
        log.info("Processing workload event: trainer={}, action={}, minutes={}", req.trainerUsername(), req.actionType(), req.trainingDuration());

        return repository.findByUsername(req.trainerUsername())
                .map(existing -> applyDelta(existing, req, year, month))
                .switchIfEmpty(Mono.defer(() -> onMissing(req, year, month)))
                .flatMap(repository::save)
                .then();
    }

    public Mono<TrainerSummaryResponse> getWorkload(String username) {
        log.debug("Fetching workload for {}", username);
        return repository.findByUsername(username)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new NoSuchElementException(
                        "The trainer with username " + username + " does not have any workload")));
    }

    private TrainerSummaryResponse toResponse(TrainerWorkload workload) {
        return new TrainerSummaryResponse(
                workload.getUsername(), workload.getFirstName(), workload.getLastName(),
                Boolean.TRUE.equals(workload.getActive()),
                workload.getYears().stream()
                        .map(y -> new YearlySummaryResponse(y.getYear(),
                                y.getMonths().stream()
                                        .map(m -> new MonthlySummaryResponse(m.getMonth(), m.getSummaryDuration()))
                                        .toList()))
                        .toList());
    }

    private TrainerWorkload applyDelta(TrainerWorkload workload, TrainerWorkloadRequest req, int year, Month month) {
        if (req.actionType() == ActionType.ADD) {
            workload.setFirstName(req.firstName());
            workload.setLastName(req.lastName());
            workload.setActive(req.isActive());
            MonthSummary m = findOrCreateMonth(workload, year, month);
            m.setSummaryDuration(m.getSummaryDuration() + req.trainingDuration());
            log.debug("Added {} min to {}/{} for {}", req.trainingDuration(), year, month, req.trainerUsername());
        } else {
            subtract(workload, year, month, req.trainingDuration());
            log.debug("Subtracted {} min from {}/{} for {}", req.trainingDuration(), year, month, req.trainerUsername());
        }
        return workload;
    }

    private Mono<TrainerWorkload> onMissing(TrainerWorkloadRequest req, int year, Month month) {
        if (req.actionType() == ActionType.ADD) {
            log.debug("No document for {}, creating a new one", req.trainerUsername());
            TrainerWorkload fresh = new TrainerWorkload(req.trainerUsername(), null, null, null, new ArrayList<>());
            return Mono.just(applyDelta(fresh, req, year, month));
        }
        log.warn("DELETE for unknown trainer {}, ignoring", req.trainerUsername());
        return Mono.empty();
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
