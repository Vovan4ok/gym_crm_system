package org.volodymyrzganiaiko.workload_service.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.volodymyrzganiaiko.workload_service.AbstractMongoIT;
import org.volodymyrzganiaiko.workload_service.domain.ProcessedMessage;
import org.volodymyrzganiaiko.workload_service.domain.TrainerWorkload;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
class TrainerWorkloadRepositoryIT extends AbstractMongoIT {
    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void clean() { repository.deleteAll(); }

    private TrainerWorkload sample() {
        return new TrainerWorkload("Tra.Iner", "Tra", "Iner", true,
                new ArrayList<>(List.of(new TrainerWorkload.YearSummary(2026,
                        new ArrayList<>(List.of(new TrainerWorkload.MonthSummary(Month.JULY, 60)))))));
    }

    @Test
    public void saveAndFindByUsername() {
        repository.save(sample());

        TrainerWorkload found = repository.findByUsername("Tra.Iner").orElseThrow();
        assertEquals("Tra", found.getFirstName());
        assertTrue(found.getActive());
        assertEquals(2026, found.getYears().get(0).getYear());
        assertEquals(Month.JULY, found.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, found.getYears().get(0).getMonths().get(0).getSummaryDuration());
    }

    @Test
    public void updateSummaryDuration() {
        repository.save(sample());
        TrainerWorkload w = repository.findByUsername("Tra.Iner").orElseThrow();
        w.getYears().get(0).getMonths().get(0).setSummaryDuration(90);
        repository.save(w);

        TrainerWorkload updated = repository.findByUsername("Tra.Iner").orElseThrow();
        assertEquals(90, updated.getYears().get(0).getMonths().get(0).getSummaryDuration());
    }

    @Test
    public void findByUsername_missing_empty() {
        assertTrue(repository.findByUsername("nobody").isEmpty());
    }

    @Test
    public void compoundNameIndexExists() {
        List<IndexInfo> indexes = mongoTemplate.indexOps(TrainerWorkload.class).getIndexInfo();
        boolean hasNameIndex = indexes.stream().anyMatch(i -> "idx_name".equals(i.getName()));
        assertTrue(hasNameIndex, "compound firstName+lastName index must exist");
    }

    @Test
    void processedMessageHasTtlIndex() {
        boolean hasTtl = mongoTemplate.indexOps(ProcessedMessage.class).getIndexInfo().stream()
                .anyMatch(i -> i.getExpireAfter().isPresent());
        assertTrue(hasTtl, "processed_messages must have a TTL index");
    }
}
