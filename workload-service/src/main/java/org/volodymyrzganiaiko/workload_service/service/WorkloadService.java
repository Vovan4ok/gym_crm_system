package org.volodymyrzganiaiko.workload_service.service;

import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import org.volodymyrzganiaiko.workload_service.dto.*;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkloadService {
    private final ConcurrentHashMap<String, TrainerWorkload> store = new ConcurrentHashMap<>();

    public void process(TrainerWorkloadRequest request) {
        int year = request.trainingDate().getYear();
        Month month = request.trainingDate().getMonth();
        store.compute(request.trainerUsername(), (username, workload) -> {
            if (request.actionType().equals(ActionType.ADD)) {
               if (workload == null) {
                   workload = new TrainerWorkload();
                   workload.setUsername(username);
               }
               workload.setFirstName(request.firstName());
               workload.setLastName(request.lastName());
               workload.setActive(request.isActive());
               workload.getMinutesByYearMonth()
                       .computeIfAbsent(year, y -> new HashMap<>())
                       .merge(month, request.trainingDuration(), Integer::sum);
            } else {
               if (workload == null) return null;
               Map<Month, Integer> monthMap = workload.getMinutesByYearMonth().get(year);
               if (monthMap == null) return workload;
               if (monthMap.isEmpty()) workload.getMinutesByYearMonth().remove(year);
               monthMap.merge(month, -request.trainingDuration(), Integer::sum);
               if (monthMap.get(month) <= 0) monthMap.remove(month);
            }
            return workload;
        });
    }

    public TrainerSummaryResponse getWorkload(String username) {
        TrainerWorkload workload = store.get(username);
        if (workload == null) throw new NoSuchElementException("The trainer with username " + username + " does not have any workload");
        return new TrainerSummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.getActive(),
                workload.getMinutesByYearMonth().entrySet().stream()
                        .map(el -> new YearlySummaryResponse(el.getKey(), el.getValue().entrySet().stream()
                                .map(elem -> new MonthlySummaryResponse(elem.getKey(), elem.getValue()))
                                .toList()))
                        .toList());
    }
}
