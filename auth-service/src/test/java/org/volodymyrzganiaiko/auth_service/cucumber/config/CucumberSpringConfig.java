package org.volodymyrzganiaiko.auth_service.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.volodymyrzganiaiko.auth_service.AbstractPostgresIT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig extends AbstractPostgresIT {
}
