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
- **Known limitation — in-memory state.** The summary lives in an in-process
  `ConcurrentHashMap`. The service therefore scales **vertically** (more
  consumer threads per instance) but **not horizontally** across instances:
  two instances would each hold a partial, divergent view, and
  `GET /api/workload/{username}` would answer inconsistently depending on
  which instance served it. Running more than one instance safely requires
  moving the state to shared storage (a database or Redis). This is an
  accepted trade-off for the current scope.