package org.volodymyrzganiaiko.workload_service.cucumber.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.volodymyrzganiaiko.workload_service.cucumber.world.ScenarioContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonSteps {
    @Autowired
    private ScenarioContext context;

    @Then("the response status is {int}")
    public void checkStatus(int statusCode) {
        assertEquals(statusCode, context.getLastResponse().getStatusCode().value());
    }
}
