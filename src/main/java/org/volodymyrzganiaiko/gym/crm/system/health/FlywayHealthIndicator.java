package org.volodymyrzganiaiko.gym.crm.system.health;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class FlywayHealthIndicator implements HealthIndicator {
    private final Flyway flyway;

    private static final Logger log =  LoggerFactory.getLogger(FlywayHealthIndicator.class);

    public FlywayHealthIndicator(Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public Health health() {
        try {
            MigrationInfoService service = flyway.info();
            Optional<MigrationInfo> optFound = Arrays.stream(service.all()).filter(m -> m.getState().isFailed()).findFirst();
            if (optFound.isPresent()) {
                log.warn("Migration service failed");
                return Health.down().withDetail("versionFailed", optFound.map(MigrationInfo::getVersion).get().getVersion()).build();
            } else if (service.current() == null) {
                log.warn("No current migration service found");
                return Health.down().withDetail("currentSchema", "empty").build();
            } else {
                log.debug("Current migration service found");
                return Health.up().withDetail("version", service.current().getVersion().getVersion()).build();
            }
        } catch (Exception e) {
            log.warn("Error while trying to read migration info {}", e.getMessage());
            return Health.down().withDetail("error", "Server error happened, check logs").build();
        }
    }
}
