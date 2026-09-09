package org.volodymyrzganiaiko.gym.crm.system.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.volodymyrzganiaiko.gym.crm.system.AbstractPostgresIT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfig extends AbstractPostgresIT {
    @MockitoBean
    private JmsTemplate jmsTemplate;
}
