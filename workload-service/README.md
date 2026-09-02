# workload-service

Consumes trainer-workload messages from ActiveMQ (`gym.workload.queue`) and
maintains a per-trainer monthly workload summary.

## Messaging
- Asynchronous, queue-based (point-to-point). Producer: `gym-service`
  (AFTER_COMMIT application event → `JmsTemplate`). Consumer: `@JmsListener`
  → `WorkloadService.process`.
- JSON payloads via `MappingJackson2MessageConverter`; the `_type` type-id
  mapping bridges the two services' `TrainerWorkloadRequest` packages.
- Invalid messages (failing bean validation) are routed to
  `gym.workload.dlq` with a `dlqReason`; messages that cannot be
  deserialized at all fall back to the broker's default `ActiveMQ.DLQ`.
- `correlationId` travels as a JMS property and is restored into the MDC for
  end-to-end tracing.

## Persistence (MongoDB)
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

Fixing the root fault-tolerance gap on the **producer** side (the AFTER_COMMIT
send can lose a message if the broker is down at that instant, since the DB
transaction has already committed) would require a transactional outbox and is
out of scope for the current iteration.

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