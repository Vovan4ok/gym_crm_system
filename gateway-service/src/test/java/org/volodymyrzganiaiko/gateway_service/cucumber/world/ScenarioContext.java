package org.volodymyrzganiaiko.gateway_service.cucumber.world;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
@Setter
@Getter
public class ScenarioContext {
    private String token;

    private int lastStatus;

    private String lastPath;
}
