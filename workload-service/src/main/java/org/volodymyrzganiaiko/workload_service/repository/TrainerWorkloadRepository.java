package org.volodymyrzganiaiko.workload_service.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;
import reactor.core.publisher.Mono;

public interface TrainerWorkloadRepository extends ReactiveMongoRepository<TrainerWorkload, String> {
    Mono<TrainerWorkload> findByUsername(String username);
}
