package org.volodymyrzganiaiko.workload_service;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class AbstractMongoIT {
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");
    static { MONGO.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }
}
