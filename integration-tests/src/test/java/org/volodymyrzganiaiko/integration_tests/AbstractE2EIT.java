package org.volodymyrzganiaiko.integration_tests;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

public class AbstractE2EIT {
    static final ComposeContainer STACK =
            new ComposeContainer(new File("../docker-compose.yml"))
                    .withLocalCompose(true)
                    .withExposedService("gateway-service", 8080,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));
    static { STACK.start(); }

    protected String gatewayUrl() {
        return "http://localhost:8080";
    }
}
