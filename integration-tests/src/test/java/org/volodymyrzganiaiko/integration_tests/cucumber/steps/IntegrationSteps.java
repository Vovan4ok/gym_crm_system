package org.volodymyrzganiaiko.integration_tests.cucumber.steps;


import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testcontainers.DockerClientFactory;
import org.volodymyrzganiaiko.integration_tests.AbstractE2EIT;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationSteps extends AbstractE2EIT {
    private static final String WORKLOAD_SERVICE = "workload-service";

    private String trainerUsername;
    private String trainerPassword;
    private String trainerToken;
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
        trainerPassword = r.jsonPath().getString("password");

        registerTraineeHelper();
    }

    // ---- gym-service workload endpoint (served over gRPC by workload-service) ----

    @When("the trainer logs in")
    public void trainerLogsIn() {
        lastResponse = given().contentType(ContentType.JSON)
                .body(Map.of("username", trainerUsername, "password", trainerPassword))
                .post("/api/login");
        trainerToken = lastResponse.jsonPath().getString("accessToken");
    }

    private Response getTrainerWorkload(String bearerToken, String username) {
        return given().header("Authorization", "Bearer " + bearerToken)
                .get("/api/trainers/" + username + "/workload");
    }

    @When("the trainer requests their workload")
    public void trainerRequestsWorkload() {
        lastResponse = getTrainerWorkload(trainerToken, trainerUsername);
    }

    @When("the trainee requests the trainer's workload")
    public void traineeRequestsTrainersWorkload() {
        lastResponse = getTrainerWorkload(token, trainerUsername);
    }

    @When("the trainee requests a workload under their own username")
    public void traineeRequestsOwnWorkload() {
        lastResponse = getTrainerWorkload(token, traineeUsername);
    }

    @Then("the workload has no entries")
    public void workloadIsEmpty() {
        assertEquals(trainerUsername, lastResponse.jsonPath().getString("username"));
        assertEquals(0, lastResponse.jsonPath().getList("years").size());
    }

    @Then("the trainer's own workload eventually shows {int} minutes")
    public void ownWorkloadEventually(int minutes) {
        // The training reaches workload-service asynchronously (outbox -> JMS), then is read back over gRPC.
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            Response r = getTrainerWorkload(trainerToken, trainerUsername);
            assertEquals(200, r.statusCode());
            // Until the training arrives the answer is 200 with an empty list, so the value is
            // read as Integer: null must fail the assertion (and be retried), not throw an NPE.
            Integer actual = r.jsonPath().get("years[0].months[0].summaryDuration");
            assertEquals(Integer.valueOf(minutes), actual);
        });
    }

    @Given("workload-service is stopped")
    public void stopWorkloadService() {
        DockerClientFactory.instance().client()
                .stopContainerCmd(service(WORKLOAD_SERVICE).getContainerId()).exec();
    }

    @Then("once workload-service is started again the workload is returned")
    public void startWorkloadServiceAndRecover() {
        startWorkloadServiceIfStopped();
        // gym-service's gRPC channel reconnects by itself; no restart of gym-service needed.
        await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() ->
                assertEquals(200, getTrainerWorkload(trainerToken, trainerUsername).statusCode()));
    }

    /** Safety net: never leave workload-service stopped for the scenarios that follow. */
    @After("@workload-down")
    public void startWorkloadServiceIfStopped() {
        String id = service(WORKLOAD_SERVICE).getContainerId();
        Boolean running = DockerClientFactory.instance().client()
                .inspectContainerCmd(id).exec().getState().getRunning();
        if (!Boolean.TRUE.equals(running)) {
            DockerClientFactory.instance().client().startContainerCmd(id).exec();
        }
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
