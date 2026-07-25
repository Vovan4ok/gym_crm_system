package org.volodymyrzganiaiko.gym.crm.system.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.TrainingTypeService;

import java.util.List;

@Component
public class TrainingTypeHealthIndicator implements HealthIndicator {
    private final TrainingTypeService service;

    private static final Logger log =  LoggerFactory.getLogger(TrainingTypeHealthIndicator.class);

    public TrainingTypeHealthIndicator(TrainingTypeService service) {
        this.service = service;
    }

    @Override
    public Health health() {
        try {
            List<TrainingType> types = service.findAll();

            if (types.isEmpty()) {
                log.warn("No training types found");
                return Health.down().withDetail("count", 0).build();
            }

            log.debug("Found {} training types", types.size());
            return Health.up().withDetail("count", types.size()).build();
        } catch (Exception e) {
            log.warn("Couldn't find any training types: {}", e.getMessage());
            return Health.down().withDetail("error", "Server error happened, check logs").build();
        }
    }
}
