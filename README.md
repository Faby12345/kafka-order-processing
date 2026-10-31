# Kafka Order Processing

A hands-on project built with Java 21, Spring Boot, and Apache Kafka to explore event-driven processing, consumer groups, retries, dead letter topics, and duplicate delivery.

## Current flow

1. A client sends POST /orders.
2. OrderService generates an order ID and an OrderCreatedEvent.
3. OrderProducer publishes the event to orders.created, using customerId as the key.
4. OrderConsumer receives the event and logs the order details.
5. After successful listener completion, Spring commits the next offset.

Kafka accepting an event and the consumer processing it are separate operations. The HTTP response confirms publication, not completed order processing.

Orders are not persisted in a database yet.

## Kafka topics

| Topic | Partitions | Replication factor | Purpose |
| --- | --- | --- | --- |
| orders.created | 2 | 1 | Order-created events |
| orders.created.DLT | 2 | 1 | Events that fail processing after retries |

Events with the same customer key route to the same partition while the partition count stays unchanged. Different keys can share a partition.

Offsets identify positions within individual partitions.

## Running locally

Requirements:

- JDK 21
- A running Kafka broker reachable at localhost:9092
- Optional Kafka UI at localhost:8081

Configure the broker address through an environment variable:

export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

Start the application:

./mvnw spring-boot:run

The API uses port 8080 by default.

A .env file is not automatically loaded by Spring Boot. Supply environment variables through your shell or IDE run configuration.

## Request scripts

Run these commands from the directory containing the scripts.

### create-order.sh

Sends one sample order to http://localhost:8080/orders and prints the HTTP response:

bash create-order.sh

### create-six-orders.sh

Sends six orders, using three customer IDs with two orders per customer:

bash create-six-orders.sh

To target another application instance:

bash create-six-orders.sh http://localhost:8082

The customer IDs are interleaved to make partition routing easier to observe. Three customer IDs do not guarantee three different partitions.

## Consumer group experiments

The default consumer group is order-processing.

Start a second instance in the same group:

./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

With two partitions and one consumer per instance, the consumers share the partitions. Stopping one instance allows the remaining consumer to take ownership of both partitions after a rebalance.

To run an independent group:

./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --spring.kafka.consumer.group-id=order-audit"

Different groups independently read the same topic and maintain separate committed offsets.

With auto-offset-reset=earliest, a group without valid committed offsets starts at the earliest retained records. An existing group resumes from its valid committed positions.

## Retries and dead letter topic

The error handler uses:

- One initial processing attempt
- Three retries, one second apart
- DeadLetterPublishingRecoverer after retries are exhausted

Product FAIL deliberately triggers a processing exception.

After retries fail, the recoverer publishes the event to orders.created.DLT, preserving the original partition. DLT headers include failure details and the original record location.

The original event remains in orders.created. Successful recovery allows the group to advance past it.

DLT publication failures are configured to fail recovery rather than silently treating it as successful.

## Duplicate delivery experiment

Product DUPLICATE_TEST pauses the listener for 20 seconds after simulated processing, before the listener returns.

To observe redelivery:

1. Run only one application instance.
2. Submit a DUPLICATE_TEST order.
3. Wait for the "Processing finished. Waiting before offset commit" log.
4. Forcefully terminate that application process with kill -9 <APP_PID>.
5. Restart the app with the same consumer group.
6. Observe the same event ID, partition, and offset again.

Only terminate the process belonging to this learning application.

The pause creates a window before the offset commit. A graceful shutdown may let processing finish, so it may not reproduce the experiment.

This illustrates why processing can happen more than once: the application can finish its business operation and crash before Kafka records its progress.

## Next steps

1. Add PostgreSQL and persist orders.
2. Add a processed-events table with a unique event ID.
3. Save the processed-event marker and order in one database transaction.
4. Skip repeated events without repeating their business effects.
5. Repeat the crash experiment and verify that only one order is stored.
6. Add a controlled workflow for replaying DLT records.
7. Explore the transactional outbox pattern for reliable database writes and event publication.

Database idempotency protects operations inside that transaction. External effects, such as payments, require their own idempotency strategy.

## Learning scope

This is a local learning project. The single-broker setup has no replica redundancy, and the deliberate failure and delay conditions are experiments rather than production business logic.
