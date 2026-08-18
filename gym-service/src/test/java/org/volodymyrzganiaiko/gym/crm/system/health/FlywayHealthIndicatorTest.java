package org.volodymyrzganiaiko.gym.crm.system.health;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FlywayHealthIndicatorTest {
    @Mock
    private Flyway flyway;

    @Mock
    private MigrationInfoService migrationInfoService;

    @Mock
    private MigrationInfo migrationInfo;

    @InjectMocks
    private FlywayHealthIndicator flywayHealthIndicator;

    @Test
    public void health_upWhenAllMigrationsSucceeded() {
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.all()).thenReturn(new MigrationInfo[]{migrationInfo});
        when(migrationInfo.getState()).thenReturn(MigrationState.SUCCESS);
        when(migrationInfoService.current()).thenReturn(migrationInfo);
        when(migrationInfo.getVersion()).thenReturn(MigrationVersion.fromVersion("4"));

        Health health = flywayHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("4", health.getDetails().get("version"));
    }

    @Test
    public void health_downWhenMigrationFailed() {
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.all()).thenReturn(new MigrationInfo[]{migrationInfo});
        when(migrationInfo.getState()).thenReturn(MigrationState.FAILED);
        when(migrationInfo.getVersion()).thenReturn(MigrationVersion.fromVersion("4"));

        Health health = flywayHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("4", health.getDetails().get("versionFailed"));
    }

    @Test
    public void health_downWhenSchemaEmpty() {
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.all()).thenReturn(new MigrationInfo[0]);
        when(migrationInfoService.current()).thenReturn(null);

        Health health = flywayHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("currentSchema"));
    }

    @Test
    public void health_downWhenFlywayThrows() {
        when(flyway.info()).thenThrow(new RuntimeException());

        Health health = flywayHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }
}
