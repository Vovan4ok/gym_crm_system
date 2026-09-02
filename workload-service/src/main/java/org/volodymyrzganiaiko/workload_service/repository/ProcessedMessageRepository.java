package org.volodymyrzganiaiko.workload_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.volodymyrzganiaiko.workload_service.domain.ProcessedMessage;

public interface ProcessedMessageRepository extends MongoRepository<ProcessedMessage, String> {
}
