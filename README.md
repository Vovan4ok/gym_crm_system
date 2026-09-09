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
```