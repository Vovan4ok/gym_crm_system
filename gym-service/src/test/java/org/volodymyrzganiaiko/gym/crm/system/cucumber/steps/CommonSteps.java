package org.volodymyrzganiaiko.gym.crm.system.cucumber.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.volodymyrzganiaiko.gym.crm.system.cucumber.world.ScenarioContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonSteps {
    @Autowired
    private ScenarioContext context;

    @Then("the response status is {int}")
    public void checkResponseStatus(int statusCode) {
        int responseStatusCode = context.getLastResponse().getStatusCode().value();

        assertEquals(statusCode, responseStatusCode);
    }
}
