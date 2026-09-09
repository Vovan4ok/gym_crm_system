package org.volodymyrzganiaiko.gym.crm.system.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.volodymyrzganiaiko.gym.crm.system.cucumber.world.ScenarioContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TrainingSteps {
    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("the trainee creates a 60-minute {string} training")
    public void traineeCreatesTraining(String trainingName) {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", context.getTraineeUsername());
        body.put("trainerUsername", context.getTrainerUsername());
        body.put("trainingName", trainingName);
        body.put("trainingDate", LocalDate.parse("2026-07-20"));
        body.put("trainingDuration", 60);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-User", context.getTraineeUsername());
        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/api/trainings", HttpMethod.POST, httpEntity, String.class);
        context.setLastResponse(response);
    }

    @When("the trainee creates a training without a date")
    public void traineeCreatesInvalidTraining() {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", context.getTraineeUsername());
        body.put("trainerUsername", context.getTrainerUsername());
        body.put("trainingDate", LocalDate.parse("2026-07-20"));
        body.put("trainingDuration", 60);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-User", context.getTraineeUsername());
        HttpEntity httpEntity = new HttpEntity(body, headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/api/trainings", HttpMethod.POST, httpEntity, String.class);
        context.setLastResponse(response);
    }

    @Then("an outbox message for the trainer exists")
    public void outboxMessageExists() {
        assertTrue(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_messages WHERE group_id = ?", Integer.class, context.getTrainerUsername()) >= 1);
    }
}
