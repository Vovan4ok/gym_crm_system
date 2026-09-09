package org.volodymyrzganiaiko.workload_service.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.volodymyrzganiaiko.workload_service.AbstractMessagingIT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig extends AbstractMessagingIT {
}
