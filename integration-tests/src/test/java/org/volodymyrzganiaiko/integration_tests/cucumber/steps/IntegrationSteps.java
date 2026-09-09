package org.volodymyrzganiaiko.integration_tests.cucumber.steps;


import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.volodymyrzganiaiko.integration_tests.AbstractE2EIT;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationSteps extends AbstractE2EIT {
    private String trainerUsername;
    private String traineeUsername;
    private String traineePassword;
    private String token;
    private Response lastResponse;

    @Before
    public void setUp() {
        RestAssured.baseURI = gatewayUrl();
    }

    private void registerTraineeHelper() {
        Response r = given().contentType(ContentType.JSON)
                .body(Map.of("firstName","Tra","lastName","Inee"))
                .post("/api/trainees");
        traineeUsername = r.jsonPath().getString("username");
        traineePassword = r.jsonPath().getString("password");
    }

    @Given("a registered trainee")
    public void registerTrainee() {
        registerTraineeHelper();
    }

    @When("the trainee logs in with a wrong password")
    public void loginWithWrongPassword() {
        lastResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", traineeUsername, "password", "WRONG"))
                .post("/api/login");
    }

    @When("a trainer and a trainee are registered")
    public void register() {
        Response r = given().contentType(ContentType.JSON)
                .body(Map.of("firstName","Tra","lastName","Iner","specializationId",2))
                .post("/api/trainers");
        trainerUsername = r.jsonPath().getString("username");

        registerTraineeHelper();
    }

    @When("the trainee logs in")
    public void login() {
        lastResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", traineeUsername, "password", traineePassword))
                .post("/api/login");
        token = lastResponse.jsonPath().getString("accessToken");
    }

    @When("the trainee creates a 60-minute training")
    public void createTraining() {
        lastResponse = given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("traineeUsername", traineeUsername, "trainerUsername", trainerUsername,
                        "trainingName","E2E","trainingDate","2026-07-20","trainingDuration",60))
                .post("/api/trainings");
    }

    @Then("the response status is {int}")
    public void checkResponse(int responseStatus) {
        assertEquals(responseStatus, lastResponse.getStatusCode());
    }

    @Then("the training is accepted")
    public void trainingAccepted() {
        assertEquals(201, lastResponse.statusCode());
    }

    @Then("the trainer workload eventually shows {int} minutes")
    public void workloadEventually(int minutes) {
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            Response r = given().header("Authorization", "Bearer " + token)
                    .get("/api/workload/" + trainerUsername);
            assertEquals(200, r.statusCode());
            assertEquals(minutes, r.jsonPath().getInt("years[0].months[0].summaryDuration"));
        });
    }
}
