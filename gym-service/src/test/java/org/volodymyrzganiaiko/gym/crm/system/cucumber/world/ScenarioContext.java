package org.volodymyrzganiaiko.gym.crm.system.cucumber.world;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
@Setter
@Getter
public class ScenarioContext {
    private ResponseEntity<String> lastResponse;

    private String traineeUsername;

    private String traineePassword;

    private String trainerUsername;
}
