package org.volodymyrzganiaiko.gym.crm.system.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GymMetricsTest {
    private MeterRegistry registry;

    private GymMetrics gymMetrics;

    @BeforeEach
    public void setUo() {
        this.registry = new SimpleMeterRegistry();
        this.gymMetrics = new GymMetrics(registry);
    }

    @Test
    public void recordRegistration_incrementsCounter() {
        gymMetrics.recordRegistration("trainee");

        double v = registry.get("gym.successful.registrations").tag("role", "trainee").counter().count();

        assertEquals(1.0, v);
    }

    @Test
    public void recordRegistration_accumulatesOnRepeatedCalls() {
        gymMetrics.recordRegistration("trainee");
        gymMetrics.recordRegistration("trainee");

        double v = registry.get("gym.successful.registrations").tag("role", "trainee").counter().count();

        assertEquals(2.0, v);
    }

    @Test
    public void recordRegistration_separatesCountersByRole() {
        gymMetrics.recordRegistration("trainee");
        gymMetrics.recordRegistration("trainer");

        double v_trainee = registry.get("gym.successful.registrations").tag("role", "trainee").counter().count();
        double v_trainer = registry.get("gym.successful.registrations").tag("role", "trainer").counter().count();

        assertEquals(1.0, v_trainee);
        assertEquals(1.0, v_trainer);
    }

    @Test
    public void recordUsernameCollision_incrementsCounter() {
        gymMetrics.recordUsernameCollision("trainee");

        double v = registry.get("gym.username.collisions").tag("role", "trainee").counter().count();

        assertEquals(1.0, v);
    }

    @Test
    public void timeTrainingCreation_recordsMeasurement() {
        gymMetrics.timeTrainingCreation(() -> {});

        Timer result = registry.get("gym.training.creation").timer();

        assertEquals(1, result.count());
        assertTrue(result.totalTime(TimeUnit.NANOSECONDS) > 0);
    }

    @Test
    public void timeTrainingCreation_executesAction() {
        AtomicBoolean ran = new AtomicBoolean(false);

        gymMetrics.timeTrainingCreation(() -> ran.set(true));

        assertTrue(ran.get());
    }
}
