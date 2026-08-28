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
- `transactionId` travels as a JMS property and is restored into the MDC for
  end-to-end tracing.

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