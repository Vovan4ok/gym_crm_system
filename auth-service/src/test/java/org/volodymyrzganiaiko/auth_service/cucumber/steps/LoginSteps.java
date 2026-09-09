package org.volodymyrzganiaiko.auth_service.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.auth_service.dto.LoginRequest;

import static org.junit.jupiter.api.Assertions.*;

public class LoginSteps {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private ResponseEntity<String> response;

    @Before
    public void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE users");
    }

    @Given("an active user {string} with password {string}")
    public void activeUser(String username, String rawPassword) {
        insertUser(username, rawPassword, true);
    }

    @Given("an inactive user {string} with password {string}")
    public void inactiveUser(String username, String rawPassword) {
        insertUser(username, rawPassword, false);
    }

    private void insertUser(String username, String rawPassword, boolean active) {
        jdbcTemplate.update(
                "INSERT INTO users(user_id, username, password, is_active) VALUES (?,?,?,?)",
                1L, username, passwordEncoder.encode(rawPassword), active);
    }

    @When("I log in as {string} with password {string}")
    public void login(String username, String password) {
        var body = new LoginRequest(username, password);
        response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/login", body, String.class);
    }

    @Then("the response status is {int}")
    public void status(int expected) {
        assertEquals(expected, response.getStatusCode().value());
    }
    @Then("an access token is returned")
    public void tokenReturned() {
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("accessToken"));
    }
}
