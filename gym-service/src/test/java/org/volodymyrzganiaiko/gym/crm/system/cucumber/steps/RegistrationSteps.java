package org.volodymyrzganiaiko.gym.crm.system.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.volodymyrzganiaiko.gym.crm.system.cucumber.world.ScenarioContext;
import org.volodymyrzganiaiko.gym.crm.system.dto.TraineeRegistrationRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RegistrationSteps {
    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Given("a registered trainee")
    public void trainee() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Tra");
        body.put("lastName", "Inee");
        body.put("dateOfBirth", LocalDate.parse("2003-11-08"));
        body.put("address", "Test address");
        Map<String, String> response = register("/api/trainees", body);
        context.setTraineeUsername(response.get("username"));
        context.setTraineePassword(response.get("password"));
    }
    @Given("a registered trainer and trainee")
    public void traineeAndTrainer() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "Tra");
        body.put("lastName", "Iner");
        body.put("specializationId", 1L);
        Map<String, String> response = register("/api/trainers", body);
        context.setTrainerUsername(response.get("username"));

        body = new HashMap<>();
        body.put("firstName", "Tra");
        body.put("lastName", "Inee");
        body.put("dateOfBirth", LocalDate.parse("2003-11-08"));
        body.put("address", "Test address");
        response = register("/api/trainees", body);
        context.setTraineeUsername(response.get("username"));
        context.setTraineePassword(response.get("password"));
    }
    private Map<String, String> register(String path, Map<String, Object> bodyMap) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + path, bodyMap, String.class);
        Map<String, String> result = new HashMap<>();
        result.put("username", objectMapper.readTree(response.getBody()).get("username").asText());
        result.put("password", objectMapper.readTree(response.getBody()).get("password").asText());
        return result;
    }

    @When("I register a trainee {string} {string}")
    public void registerTrainee(String firstName, String lastName) throws Exception {
        var body = new TraineeRegistrationRequest(firstName, lastName, LocalDate.parse("2003-11-08"), "Test address");
        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:" + port + "/api/trainees", body, String.class);
        context.setLastResponse(response);
        context.setTraineeUsername(objectMapper.readTree(response.getBody()).get("username").asText());
        context.setTraineePassword(objectMapper.readTree(response.getBody()).get("password").asText());
    }

    @Then("a username and password are returned")
    public void credentialsReturned() throws Exception {
        ResponseEntity<String> response = context.getLastResponse();

        assertNotNull(objectMapper.readTree(response.getBody()).get("username").asText());
        assertNotNull(objectMapper.readTree(response.getBody()).get("password").asText());
        assertNotNull(context.getTraineeUsername());
        assertNotNull(context.getTraineePassword());
    }
}