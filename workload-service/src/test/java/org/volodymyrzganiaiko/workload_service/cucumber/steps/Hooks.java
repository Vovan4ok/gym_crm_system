package org.volodymyrzganiaiko.workload_service.cucumber.steps;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.volodymyrzganiaiko.workload_service.repository.ProcessedMessageRepository;
import org.volodymyrzganiaiko.workload_service.repository.TrainerWorkloadRepository;

public class Hooks {
    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Before
    public void clean() {
        trainerWorkloadRepository.deleteAll().block();
        processedMessageRepository.deleteAll().block();
    }
}
