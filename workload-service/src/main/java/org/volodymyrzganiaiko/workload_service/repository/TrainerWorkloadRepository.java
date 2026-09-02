package org.volodymyrzganiaiko.workload_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;

import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {
    Optional<TrainerWorkload> findByUsername(String username);
}
