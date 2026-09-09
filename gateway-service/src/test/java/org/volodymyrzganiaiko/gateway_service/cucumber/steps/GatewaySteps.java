package org.volodymyrzganiaiko.gateway_service.cucumber.steps;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.volodymyrzganiaiko.gateway_service.AbstractGatewayIT;
import org.volodymyrzganiaiko.gateway_service.cucumber.world.ScenarioContext;

import java.time.Instant;
import java.util.Date;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GatewaySteps {
    @LocalServerPort
    private int port;

    @Autowired
    private ScenarioContext context;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Given("a valid token for {string}")
    public void validToken(String subject) throws Exception {
        JWSSigner signer = new RSASSASigner(AbstractGatewayIT.RSA_KEY.toRSAKey());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .build();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(AbstractGatewayIT.RSA_KEY.getKeyID()).build(),
                claims);
        jwt.sign(signer);
        context.setToken(jwt.serialize());
    }

    @When("I GET {string} with the token")
    public void getWithToken(String path) {
        int status = client().get().uri(path)
                .header("Authorization", "Bearer " + context.getToken())
                .exchange()
                .returnResult(Void.class).getStatus().value();
        context.setLastStatus(status);
        context.setLastPath(path);
    }

    @When("I GET {string} without a token")
    public void getNoToken(String path) {
        int status = client().get().uri(path)
                .exchange().returnResult(Void.class).getStatus().value();
        context.setLastStatus(status);
        context.setLastPath(path);
    }

    @When("I GET {string} with the token and X-Auth-User {string}")
    public void getWithTokenAndSpoof(String path, String spoof) {
        int status = client().get().uri(path)
                .header("Authorization", "Bearer " + context.getToken())
                .header("X-Auth-User", spoof)              // клієнтський — gateway має зрізати
                .exchange().returnResult(Void.class).getStatus().value();
        context.setLastStatus(status);
        context.setLastPath(path);
    }

    @When("I POST {string} without a token")
    public void postNoToken(String path) {
        int status = client().post().uri(path)
                .exchange().returnResult(Void.class).getStatus().value();
        context.setLastStatus(status);
        context.setLastPath(path);
    }

    @Then("the gateway responds {int}")
    public void status(int expected) {
        assertEquals(expected, context.getLastStatus());
    }

    @Then("downstream received X-Auth-User {string}")
    public void forwarded(String expected) {
        AbstractGatewayIT.WIREMOCK.verify(
                getRequestedFor(urlPathEqualTo(context.getLastPath()))
                        .withHeader("X-Auth-User", equalTo(expected)));
    }
}
