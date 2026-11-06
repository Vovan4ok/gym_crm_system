package org.volodymyrzganiaiko.integration_tests;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;

public class AbstractE2EIT {
    /** A free host port for the gateway, so the stack starts even when 8080 is taken. */
    static final int GATEWAY_PORT = freePort();

    static final ComposeContainer STACK =
            new ComposeContainer(new File("../docker-compose.yml"))
                    .withLocalCompose(true)
                    .withEnv("GATEWAY_PORT", String.valueOf(GATEWAY_PORT))
                    .withExposedService("gateway-service", 8080,
                            Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));
    static { STACK.start(); }

    protected String gatewayUrl() {
        return "http://localhost:" + GATEWAY_PORT;
    }

    /** One container of the running stack, e.g. to stop and start it in a scenario. */
    protected static ContainerState service(String serviceName) {
        return STACK.getContainerByServiceName(serviceName)
                .orElseThrow(() -> new IllegalStateException("No running compose service " + serviceName));
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
