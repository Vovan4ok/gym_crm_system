package org.volodymyrzganiaiko.gym.crm.system.cucumber.steps;

import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.volodymyrzganiaiko.gym.crm.system.cucumber.world.ScenarioContext;

public class ProfileSteps {
    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @When("user {string} deletes trainee's profile")
    public void userDeletesTraineesProfile(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-User", username);
        context.setLastResponse(restTemplate.exchange("http://localhost:" + port + "/api/trainees/" + context.getTraineeUsername(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class));

    }
}
