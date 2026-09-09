package org.volodymyrzganiaiko.workload_service.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.volodymyrzganiaiko.workload_service.cucumber.world.ScenarioContext;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

public class QuerySteps {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioContext scenarioContext;

    @Then("the {string} summary eventually shows {int} minutes")
    public void summaryEventually(String trainerUsername, int minutes) {
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-Auth-User", trainerUsername);
                    restTemplate.exchange(
                            "http://localhost:" + port + "/api/workload/" + trainerUsername,
                            HttpMethod.GET, new HttpEntity<>(headers), String.class);
                });
    }

    @When("I request the workload of {string}")
    public void requestWorkload(String trainerUsername) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-User", trainerUsername);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/workload/" + trainerUsername,
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
        scenarioContext.setLastResponse(response);
    }
}
