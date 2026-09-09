package org.volodymyrzganiaiko.gateway_service.cucumber.steps;

import io.cucumber.java.Before;
import org.volodymyrzganiaiko.gateway_service.AbstractGatewayIT;

public class Hooks {
    @Before
    public void clean() {
        AbstractGatewayIT.WIREMOCK.resetRequests();
    }
}
