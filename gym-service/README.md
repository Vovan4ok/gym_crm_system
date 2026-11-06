# gym-service

Core CRM service: trainees, trainers, trainings, authentication. Publishes
trainer-workload changes to `workload-service` asynchronously over ActiveMQ,
and reads a trainer's workload back from it synchronously over gRPC.

## Workload messaging — transactional outbox

The service never sends a JMS message directly from a request thread. Instead
it uses the **transactional outbox** pattern so a workload notification can
never be lost, even if the broker is down at the moment of the change.

1. **Write.** When a training is created or deleted (or a trainee with
   trainings is removed), `GymFacade` — inside the **same database
   transaction** as the data change — serializes a `TrainerWorkloadRequest` to
   JSON and inserts an `outbox_messages` row (`OutboxDAO`). Training row and
   outbox row therefore commit atomically: if the transaction rolls back, no
   phantom message; if it commits, the message is durably queued.
2. **Relay.** `OutboxRelay` (`@Scheduled`, every `messaging.outbox.poll-delay`
   ms) reads a batch of `PENDING` rows, publishes each to
   `messaging.workload-queue` via `JmsTemplate`, and marks it `SENT`. Each
   message carries `correlationId`, `JMSXGroupID = trainerUsername` (ordering
   per trainer), and `messageId = outbox row id` (the consumer's de-dup key).
3. **Retry on failure.** If the broker is unavailable, the send throws, the row
   stays `PENDING`, its `attempts` counter is incremented, and the next relay
   tick retries. No message is dropped — it simply waits in the database until
   delivery succeeds.

This gives **at-least-once** delivery. A duplicate is possible (the relay may
send a message and then fail before marking it `SENT`), so the consumer is
idempotent — it de-duplicates by `messageId`. See `workload-service/README.md`.

### Why not send after commit?
An `AFTER_COMMIT` event send loses the message if the broker is down at that
instant (the DB has already committed). Sending *before* commit only moves the
failure window (a send can succeed and the commit then roll back → phantom
message). Without XA there is no atomic DB+JMS commit, so the outbox — a single
local transaction plus an asynchronous relay — is the correct trade-off.

### Configuration
```yaml
messaging:
  workload-queue: gym.workload.queue
  outbox:
    poll-delay: 5000   # relay tick, ms
    batch-size: 100    # rows per tick
```

## Authentication
gym-service sits behind the API gateway and does **not** validate JWTs or fetch
JWKS. The gateway authenticates the request and forwards the username in a
trusted `X-Auth-User` header; a servlet filter turns it into the
`SecurityContext`, so `@PreAuthorize("#username == authentication.name")`
ownership checks keep working. This trust is safe only because gym-service is
not published to the host — it is reachable only through the gateway.
auth-service verifies login credentials directly against the shared database,
so gym-service exposes no credential-verification endpoint.

## Workload query over gRPC
`GET /api/trainers/{username}/workload` (only the trainer themselves,
`@PreAuthorize`) returns the trainer's minutes per year and month. The data
lives in workload-service, which gym-service calls over gRPC (contract:
`grpc-contract`).

**Why gRPC here, and JMS for the rest.** Commands (a training added or
removed) are asynchronous and must never be lost, hence the outbox and JMS
above. Reading a workload is a query: the caller waits for the answer, so it
is synchronous. For that internal call gRPC gives a typed contract generated
on both sides, a compact binary encoding over one HTTP/2 connection, and
first-class deadlines and status codes. External clients keep talking JSON
over HTTP to the gateway (browsers do not speak gRPC natively); gRPC is used
only between services.

```
TrainerController → GymFacade.getTrainerWorkload
    1. trainerService.findByUsername ─ unknown trainer → 404
    2. WorkloadClient.findWorkload   ─ gRPC → workload-service:9090
         OK         → the workload
         NOT_FOUND  → trainer has no trainings yet → 200 with "years": []
         otherwise  → WorkloadUnavailableException → 503
```

- **Anti-corruption layer.** The facade depends on the `client.WorkloadClient`
  interface; `GrpcWorkloadClient` is the only class that sees gRPC types, and
  it maps protobuf messages to the `TrainerWorkloadResponse` DTO.
- **"Not found" depends on context.** workload-service creates a trainer's
  document on the first training, so its `NOT_FOUND` for an existing trainer
  means "no workload yet". gym-service checks its own database first: only a
  trainer unknown to gym gets a 404.
- **No database transaction around the remote call.** `getTrainerWorkload` is
  deliberately not `@Transactional`: the trainer lookup runs in its own short
  read-only transaction, so a slow workload-service cannot hold a pooled
  database connection.
- **Deadline on every call.** `workload.grpc.deadline` (2s). A gRPC deadline is
  an absolute point in time, so it is set per call
  (`stub.withDeadlineAfter(...)`), never once on the shared stub, where it
  would expire for every later call.
- **Failure is fast and local.** The channel connects lazily, so gym-service
  starts without workload-service (no `depends_on` on it in compose). While workload-service is
  down only this endpoint fails, with a 503 in milliseconds (`UNAVAILABLE`),
  and the channel reconnects by itself when it is back.
- **Forward compatibility.** Months are read by their wire number
  (`getMonthValue()`): a value this client's contract version does not know is
  skipped with a warning instead of failing the request.

### Configuration
```yaml
spring:
  grpc:
    client:
      channels:
        workload:
          address: ${WORKLOAD_GRPC_ADDRESS:static://localhost:9090}  # compose: static://workload-service:9090
workload:
  grpc:
    deadline: 2s
```
`src/test/resources/application.yml` **replaces** (does not merge with) the
main one, so new properties must be repeated there.

## Correlation id
An inbound `X-Correlation-Id` header is honoured (a full UUID is generated when
absent), echoed on the response, carried across the outbox row and the JMS
message, and restored into the MDC on the consumer — so one id is greppable
across both services' logs for a single request. gRPC calls carry it too:
`client.CorrelationIdClientInterceptor` copies it from the MDC into the
`x-correlation-id` call metadata (gRPC's headers), and workload-service puts it
back into its MDC.
