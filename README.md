# Gym CRM System

Spring Boot 3.5 microservices (Java 19, Maven multi-module) for a gym:
trainees, trainers, trainings and each trainer's monthly workload.

## Architecture
```
client ──HTTP/JSON──► gateway-service (validates the JWT)
                              │ HTTP
            ┌─────────────────┼──────────────────────────┐
            ▼                 ▼                          ▼
      auth-service       gym-service ──JMS: commands──► workload-service
            │                 │  ──gRPC: queries─────►        │
            ▼                 ▼                               ▼
            └──► PostgreSQL ◄─┘                            MongoDB
```

| Module | Role |
|---|---|
| `gateway-service` | The only public entry point (Spring Cloud Gateway); validates the JWT and forwards a trusted identity header |
| `auth-service` | Issues JWT access and refresh tokens |
| `gym-service` | Trainees, trainers, trainings (PostgreSQL, Flyway) |
| `workload-service` | Per-trainer monthly workload (reactive MongoDB) |
| `grpc-contract` | The shared `.proto` contract and generated gRPC code |
| `integration-tests` | End-to-end Cucumber tests against the whole Docker stack |

Each style of communication is chosen for its job:

- **HTTP/JSON at the edge.** Clients talk to the gateway only.
- **Asynchronous commands over JMS (ActiveMQ).** A training added or removed
  must never be lost: a transactional outbox in gym-service, at-least-once
  delivery, and an idempotent consumer in workload-service.
- **Synchronous queries over gRPC.** gym-service reads a trainer's workload
  from workload-service through a typed protobuf contract, with per-call
  deadlines, status-code mapping and correlation-id propagation. The gRPC port
  is internal only.

Details: [`gym-service`](gym-service/README.md),
[`workload-service`](workload-service/README.md),
[`gateway-service`](gateway-service/README.md),
[`grpc-contract`](grpc-contract/README.md).

### Running the stack
```bash
docker compose up --build          # gateway on http://localhost:8080
GATEWAY_PORT=18080 docker compose up --build   # when port 8080 is taken
```

## Running the tests

Prerequisites: JDK 19, Maven, and **Docker running** (component & integration
tests use Testcontainers).

### Test layout
- **Unit tests** — `*Test.java`, run in the **surefire** phase (`mvn test`), no Docker.
- **Component tests** — Cucumber/BDD per service (`RunCucumberIT`, `*IT.java`),
  run in the **failsafe** phase (`mvn verify`); use Testcontainers / WireMock.
- **Integration tests** — cross-service `integration-tests` module: the whole
  `docker-compose` stack via Testcontainers `ComposeContainer`, driven through
  the gateway with RestAssured.

### Common commands
```bash
# Only unit tests (fast, no Docker) — whole project
mvn test

# Everything: unit + component + integration (needs Docker)
mvn verify

# Unit + component for ONE service
mvn -pl gym-service verify

# Skip the slow integration/component phase, unit only
mvn verify -DskipITs
```

### Selecting a specific set
```bash
# A single unit test class
mvn -pl gym-service test -Dtest=TrainingServiceTest

# A single unit test method
mvn -pl gym-service test -Dtest=TrainingServiceTest#createsTraining

# The component (Cucumber) tests of one service — one endpoint's behaviour
mvn -pl auth-service verify -Dit.test=RunCucumberIT

# Only the cross-service end-to-end tests
mvn -pl integration-tests verify

# Only the end-to-end scenarios of the gRPC workload query
mvn -pl integration-tests verify -Dcucumber.filter.tags=@grpc
```

The end-to-end tests start the stack on a free host port, so they run even
when something else already listens on 8080. The `@workload-down` scenario
stops and restarts the workload-service container to check that gym-service
answers 503 and then recovers on its own.