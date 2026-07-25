package org.volodymyrzganiaiko.gym.crm.system.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {
    private final MeterRegistry meterRegistry;

    public GymMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRegistration(String role) {
        Counter.builder("gym.successful.registrations")
                .description("Number of successful registrations")
                .tag("role", role)
                .register(this.meterRegistry).increment();
    }

    public void recordUsernameCollision(String role) {
        Counter.builder("gym.username.collisions")
                .description("Number of username collisions")
                .tag("role", role)
                .register(this.meterRegistry).increment();
    }

    public void timeTrainingCreation(Runnable action) {
        Timer.builder("gym.training.creation")
                .description("Time taken to create a training")
                .register(meterRegistry)
                .record(action);
    }
}
