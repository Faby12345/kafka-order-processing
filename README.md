# Kafka Order Processing

A hands-on project built with Java 21, Spring Boot, Apache Kafka, PostgreSQL, and Testcontainers. It explores event-driven processing, consumer groups, retries, dead-letter topics, duplicate delivery, and database-backed idempotency.

## Current flow

1. A client sends `POST /orders`.
2. `OrderService` saves the order in PostgreSQL.
3. It creates an `OrderCreatedEvent` with a unique event ID.
4. `OrderProducer` publishes the event to `orders.created`, using `customerId` as the Kafka key.
5. `OrderConsumer` receives the record and invokes `OrderEventProcessor`.
6. The processor checks whether the event ID already exists in `processed_event`.
7. A new event is associated with its order and recorded as processed in one database transaction. A repeated event ID is skipped.
8. After the listener completes successfully, Spring commits the next Kafka offset.

The HTTP response confirms that the order was saved and Kafka accepted the send. It does not mean that a consumer has already processed the event.

The database write and Kafka publication performed by `OrderService` are not one atomic operation. A transactional outbox is a possible future improvement for that boundary.

## Idempotent event processing

Kafka provides at-least-once delivery, so a consumer can receive the same record again if it finishes its database work but stops before its offset is committed.

`OrderEventProcessor` handles sequential redelivery as follows:

- `ProcessedEvent.eventId` is the primary key.
- The processor checks `processedEventRepository.existsById(eventId)` before doing its work.
- The processed-event marker is saved only after the order is found.
- `@Transactional` keeps the processor's database operations in one transaction.
- If processing throws, the transaction rolls back and Kafka can retry the event.
- If the same event ID is delivered again after success, the processor returns without repeating the work.

The current `processed_event.order_id` mapping is also unique, so only one processed event can be associated with an order. Remove that uniqueness constraint if the project later processes multiple event types for the same order, such as `OrderPaid` and `OrderShipped`.

## Kafka topics

| Topic | Partitions | Replication factor | Purpose |
| --- | ---: | ---: | --- |
| `orders.created` | 2 | 1 | Order-created events |
| `orders.created.DLT` | 2 | 1 | Events that still fail after retries |

Events with the same customer key route to the same partition while the partition count stays unchanged. Different keys can still share a partition. Offsets identify positions within individual partitions.

## Running locally

Requirements:

- JDK 21
- Docker, for PostgreSQL and integration tests
- A Kafka broker reachable at `localhost:9092`
- Optional Kafka UI at `localhost:8081`

Start PostgreSQL:

```bash
docker compose up -d postgres
```

The Compose file exposes PostgreSQL on `localhost:5433` and creates the `kafka_orders` database.

Configure Kafka if the broker is not already supplied through the IDE or shell:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

The matching Spring property must be spelled `spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`.

A `.env` file is not automatically loaded by Spring Boot. Supply environment variables through the shell or the IDE run configuration.

Start the application:

```bash
./mvnw spring-boot:run
```

The API uses port `8080` by default.

## Request scripts

Run the scripts from the project directory:

```bash
bash create-order.sh
```

This creates a normal order that should be persisted and processed successfully.

```bash
bash create-order-flaky.sh
```

The `FLAKY` product fails twice in memory and succeeds on the third attempt, demonstrating retry behavior.

```bash
bash create-order-fail.sh
```

The `FAIL` product always throws during consumption and is sent to the dead-letter topic after retries are exhausted.

```bash
bash create-six-orders.sh
```

This sends six orders with three interleaved customer IDs, making partition routing and consumer-group behavior easier to observe. An optional first argument selects another application instance:

```bash
bash create-six-orders.sh http://localhost:8082
```

`create-orders-same-id.sh` uses the same **customer ID**, not the same event ID. It demonstrates Kafka-key routing and does not simulate duplicate event delivery; each HTTP request still creates a new order and event.

## Consumer group experiments

The default consumer group is `order-processing`.

Start a second instance in the same group:

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=8082"
```

With two partitions and one consumer per instance, the instances share the partitions. Stopping one instance causes the remaining consumer to take ownership of both partitions after a rebalance.

Run an independent consumer group with:

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=8082 --spring.kafka.consumer.group-id=order-audit"
```

Different groups independently read the same topic and maintain separate committed offsets. With `auto-offset-reset=earliest`, a group without valid committed offsets starts at the earliest retained records; an existing group resumes from its committed positions.

## Retries and dead-letter processing

The error handler uses:

- One initial processing attempt
- Up to three retries, one second apart
- `DeadLetterPublishingRecoverer` after retries are exhausted

`FAIL` deliberately fails every attempt. `FLAKY` fails its first two attempts and then succeeds. When retries are exhausted, the recoverer publishes the record to `orders.created.DLT` on the same partition. The DLT headers contain failure information and the original record location.

The original event remains in `orders.created`; successful recovery allows the consumer group to advance past it. A DLT publication failure is configured to fail recovery instead of being treated as success.

## Tests

### Unit tests

`OrderEventProcessorTest` uses mocked repositories and does not start Spring, Kafka, Docker, or a database. It verifies the processor's control flow:

- The same event is processed only once.
- An event already marked as processed is skipped.
- A missing order does not create a processed-event marker.

Run it with:

```bash
./mvnw -Dtest=OrderEventProcessorTest test
```

### PostgreSQL integration tests

`OrderEventProcessorIntegrationTest` starts PostgreSQL 17 with Testcontainers and uses real Spring Data JPA repositories. It verifies that the marker is truly persisted, found on the second delivery, and not persisted when the order is missing.

Docker must be running. Execute:

```bash
./mvnw -Dtest=OrderEventProcessorIntegrationTest test
```

Run the complete test suite with:

```bash
./mvnw test
```

The full application-context test uses the application's configured services, while the processor integration test supplies its own isolated PostgreSQL container.

## Next steps

1. Replace the separate order-save and Kafka-send operations with a transactional outbox.
2. Add an end-to-end test that publishes duplicate Kafka records and verifies the database result.
3. Make concurrent duplicate handling explicit rather than relying only on a check followed by an insert.
4. Add a controlled workflow for inspecting and replaying DLT records.
5. Remove the unique `order_id` constraint when multiple event types per order are introduced.

Database idempotency protects operations within the same database transaction. External effects such as payment requests need their own idempotency strategy.

## Learning scope

This is a local learning project. The single-broker setup has no replica redundancy, and the deliberate failure behavior is for experimentation rather than production business logic.
