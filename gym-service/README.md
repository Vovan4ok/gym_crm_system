# gym-service

Core CRM service: trainees, trainers, trainings, authentication. Publishes
trainer-workload changes to `workload-service` asynchronously over ActiveMQ.

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

## Correlation id
An inbound `X-Correlation-Id` header is honoured (a full UUID is generated when
absent), echoed on the response, carried across the outbox row and the JMS
message, and restored into the MDC on the consumer — so one id is greppable
across both services' logs for a single request.
