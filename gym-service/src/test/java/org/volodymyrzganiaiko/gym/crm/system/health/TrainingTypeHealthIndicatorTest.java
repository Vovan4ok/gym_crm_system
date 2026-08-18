package org.volodymyrzganiaiko.gym.crm.system.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.volodymyrzganiaiko.gym.crm.system.domain.TrainingType;
import org.volodymyrzganiaiko.gym.crm.system.service.TrainingTypeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypeHealthIndicatorTest {
    @Mock
    private TrainingTypeService service;

    @InjectMocks
    TrainingTypeHealthIndicator indicator;

    @Test
    public void health_upWhenTypesExist() {
        when(service.findAll()).thenReturn(List.of(new TrainingType(), new TrainingType()));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2,  health.getDetails().get("count"));
    }

    @Test
    public void health_downWhenNoTypes() {
        when(service.findAll()).thenReturn(List.of());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(0,  health.getDetails().get("count"));
    }

    @Test
    public void health_downWhenServiceThrows() {
        when(service.findAll()).thenThrow(new RuntimeException());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
    }
}
