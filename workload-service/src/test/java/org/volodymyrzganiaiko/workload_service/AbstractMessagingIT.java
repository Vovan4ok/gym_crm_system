package org.volodymyrzganiaiko.workload_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public class AbstractMessagingIT extends AbstractMongoIT {
    static final GenericContainer<?> ACTIVEMQ =
            new GenericContainer<>("apache/activemq-classic:5.18.3").withExposedPorts(61616);
    static { ACTIVEMQ.start(); }

    @DynamicPropertySource
    static void brokerProps(DynamicPropertyRegistry r) {
        r.add("spring.activemq.broker-url",
                () -> "tcp://" + ACTIVEMQ.getHost() + ":" + ACTIVEMQ.getMappedPort(61616));
    }
}
