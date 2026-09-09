package org.volodymyrzganiaiko.gateway_service.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.volodymyrzganiaiko.gateway_service.AbstractGatewayIT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig extends AbstractGatewayIT {
}
