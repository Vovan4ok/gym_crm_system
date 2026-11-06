# workload-service

Consumes trainer-workload messages from ActiveMQ (`gym.workload.queue`) and
maintains a per-trainer monthly workload summary. The summary is readable over
REST (`GET /api/workload/{username}`, via the gateway) and over an internal
**gRPC** API used by gym-service (see [gRPC API](#grpc-api)).

## Messaging
- Asynchronous, queue-based (point-to-point). Producer: `gym-service`
  (transactional outbox → `OutboxRelay` → `JmsTemplate`, see
  `gym-service/README.md`). Consumer: `@JmsListener` → `WorkloadService.process`.
- JSON payloads via `MappingJackson2MessageConverter`; the `_type` type-id
  mapping bridges the two services' `TrainerWorkloadRequest` packages.
- Invalid messages (failing bean validation) are routed to
  `gym.workload.dlq` with a `dlqReason`; messages that cannot be
  deserialized at all fall back to the broker's default `ActiveMQ.DLQ`.
- `correlationId` travels as a JMS property and is restored into the MDC for
  end-to-end tracing.

## Persistence (MongoDB, reactive)
Persistence uses **reactive** Spring Data MongoDB (`ReactiveMongoRepository`,
`Mono`/`Flux`): `WorkloadService` composes the read-modify-save flow as a
`Mono`, and the store's de-dup checks are reactive too. Note the **consumer
stays blocking** — ActiveMQ Classic + JMS (`@JmsListener`) has no reactive
consumption, so the listener composes the reactive chain and `.block()`s it,
which keeps JMS acknowledgement, redelivery, DLQ and idempotency intact (a
message is only acked after its `Mono` completes; an error rolls back and
redelivers). The web layer is likewise servlet MVC — the controller blocks the
service `Mono` at the edge. The reactive part is the MongoDB access, not the
transport.

The per-trainer summary is stored as a MongoDB document (`trainer_workloads`),
keyed by username, with the year/month breakdown as nested lists and a compound
index on first + last name. `TrainerWorkloadRepository` (Spring Data Mongo)
exposes `findByUsername`; `WorkloadService` reads the document, adds/subtracts
the training duration for the right year/month, and saves it back. Index
creation is enabled via `spring.data.mongodb.auto-index-creation: true`.

## Idempotency (de-duplication)
The producer uses a transactional outbox with **at-least-once** delivery, so
the same message can legitimately arrive more than once (the relay may publish
a message and then fail before marking it sent). Processing a workload delta
twice would double-count minutes, so the consumer de-duplicates:

- every message carries `messageId` — the producer's outbox row id, a stable
  key (unlike `JMSMessageID`, which the broker reassigns);
- `ProcessMessageStore` records each processed id in a MongoDB
  `processed_messages` collection (a 24h TTL index expires old entries);
  the listener skips a message whose id it has already seen;
- an id is recorded **only after** `WorkloadService.process` succeeds — so a
  transient failure (which triggers broker redelivery, see below) is not
  mistaken for a duplicate and dropped.

Verified end-to-end: replaying the same `messageId` logs `Duplicate ... skipping`
and leaves the monthly total unchanged.

## Authentication
workload-service sits behind the API gateway and does **not** validate JWTs.
The gateway authenticates the request and forwards a trusted `X-Auth-User`
header, which a servlet filter turns into the `SecurityContext`; a request
without it gets 401. There are no ownership checks here — the header only marks
the request as authenticated. This trust is safe because workload-service is
not published to the host and is reachable only through the gateway.

## gRPC API
An internal, synchronous query API for gym-service, defined in `grpc-contract`
(`WorkloadService.GetWorkload`, unary). Commands (training added/removed) stay
asynchronous over JMS; this is the read path.

- **Server.** Spring gRPC (`spring-grpc-server-spring-boot-starter`) runs a
  Netty gRPC server on its own port, `${GRPC_SERVER_PORT:9090}`, next to Tomcat
  on 8081. `rpc/WorkloadGrpcService` extends the generated
  `WorkloadServiceGrpc.WorkloadServiceImplBase`; as a bean it is registered
  automatically. `rpc/WorkloadProtoMapper` turns the REST summary DTO into the
  protobuf messages.
- **Non-blocking.** Unlike the REST controller, the gRPC endpoint does not
  `.block()`: it subscribes to `WorkloadService.getWorkload`'s `Mono` and
  pushes the result into the `StreamObserver`, so no gRPC thread waits on
  MongoDB. If the client cancels or its deadline expires, a cancel handler
  disposes the subscription and the query stops.
- **Status codes.**

  | Situation | Status |
  |---|---|
  | blank `trainer_username` | `INVALID_ARGUMENT` (MongoDB is not queried) |
  | no workload document for the trainer | `NOT_FOUND` |
  | anything else | `INTERNAL`, description only "Internal error"; details are logged, never sent |

- **Correlation id.** `rpc/CorrelationIdServerInterceptor`
  (`@GlobalServerInterceptor`) reads `x-correlation-id` from the call metadata
  (or generates a UUID), puts it in the MDC and echoes it in the response
  headers. gRPC may run a call's steps (start, message, half-close, …) on
  different threads, so the MDC is set around each step and the previous value
  restored afterwards. The `Mono`'s callbacks run on a MongoDB driver thread, so
  `WorkloadGrpcService` captures the id and restores it for its error log.
- **Security.** The gRPC port is not covered by Spring Security (that guards
  Tomcat's HTTP requests only) and has no authentication of its own. It is safe
  because it is only reachable on the internal Docker network: it is listed
  under `expose`, never under `ports`, in `docker-compose.yml`.
- **Operations.** `spring.grpc.server.shutdown-grace-period: 5s` lets in-flight
  calls finish on shutdown while staying below Docker's 10s stop timeout. The
  prod profile sets `spring.grpc.server.reflection.enabled: false`. Reflection
  is inactive today (it needs `io.grpc:grpc-services`, which is not on the
  classpath), but the property defaults to `true`, so this keeps it off if that
  library is added later.
- **Tests.** Several Spring test contexts can be alive at once, so failsafe sets
  `spring.grpc.server.port=0` (a free port per context). `WorkloadGrpcServiceTest`
  uses the in-process transport (`grpc-inprocess`, no network); `WorkloadGrpcIT`
  boots the application with a real MongoDB and calls it over real Netty.

Calling it by hand from inside the Docker network (no reflection, so the
`.proto` is supplied):
```bash
docker run --rm --network gym_crm_system_default \
  -v "$PWD/grpc-contract/src/main/proto:/protos:ro" fullstorydev/grpcurl:v1.9.3 \
  -plaintext -import-path /protos -proto workload.proto \
  -d '{"trainer_username":"Tra.Iner"}' \
  workload-service:9090 org.volodymyrzganiaiko.workload_service.v1.WorkloadService/GetWorkload
```

## Error handling and retries
Message failures fall into two classes that are handled deliberately
differently — retrying is only ever applied to the first:

- **Transient failures** (broker hiccup, a temporarily unavailable
  dependency, a lock contention) — the listener lets the exception propagate,
  the JMS session rolls back, and the broker **redelivers**. The redelivery
  behaviour is set explicitly on the ActiveMQ connection factory via an
  `ActiveMQConnectionFactoryCustomizer` (see `JmsConfig`): **3 redeliveries**
  with **exponential back-off** (1s → 2s → 4s). Once the attempts are
  exhausted the broker moves the message to its default dead-letter queue,
  **`ActiveMQ.DLQ`**.
- **Poison / permanent failures** (a payload that fails bean validation —
  e.g. required information missing) — retrying would loop forever and block
  the queue, so the message is **not** retried. The listener acknowledges it
  and routes it straight to **`gym.workload.dlq`** with a `dlqReason`
  property. Messages that cannot even be deserialized fall back to the
  broker's `ActiveMQ.DLQ`.

**Two dead-letter queues, on purpose:**

| Queue | Holds | How it gets there |
|---|---|---|
| `gym.workload.dlq` | Structurally invalid payloads | Routed by the listener immediately (ACK, no retry) |
| `ActiveMQ.DLQ` | Messages that failed *processing* after all redeliveries, and undeserializable messages | Moved by the broker after redeliveries are exhausted |

On the **producer** side, a message cannot be lost when the broker is down:
gym-service writes it to an outbox table in the same transaction as the data
change and a relay retries until delivery succeeds (see `gym-service/README.md`).

## Scaling and state
- **Consumer concurrency.** The listener runs a pool of 3–10 competing
  consumers (`concurrency = "3-10"`), so one instance processes messages in
  parallel. The producer stamps `JMSXGroupID = trainerUsername` (ActiveMQ
  message groups), pinning all of a trainer's messages to the same consumer
  in order — parallelism across trainers, ordering within a trainer.
- **Shared state in MongoDB.** Both the per-trainer summary
  (`trainer_workloads`) and the de-duplication set (`processed_messages`) live
  in MongoDB, not in process memory. The service therefore scales
  **horizontally**: several instances share one consistent view, and
  `GET /api/workload/{username}` answers the same regardless of which instance
  serves it. `JMSXGroupID` still pins each trainer's messages to a single
  consumer for in-order processing.