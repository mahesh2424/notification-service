# Notification Service

Spring Boot Kafka consumer that handles reservation lifecycle events and writes
an idempotent notification log. The current implementation simulates delivery;
it does not send real email.

## Run and test

Requirements: Java 21. From this repository's root:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The default profile expects PostgreSQL at `localhost:5432`; Kafka is disabled
by default. Enable Kafka when a broker is available. For an H2-only
process/context check with Kafka disabled:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

The service listens on port `8082`. Provide its infrastructure independently;
this repository does not rely on a root Maven aggregator or shared Compose
project.

The Kafka event shape is represented by
`src/main/java/com/mahesh/flashsale/messaging/ReservationDomainEvent.java`.
Its fields must remain compatible with the event published by the booking
service.

## Build a container

```powershell
docker build -t flashsale-notification .
```

This folder is a standalone repository. From its root, the Maven Wrapper can
build and test it without a parent project or sibling service folders.

## Learning sequence

1. **Event contract:** understand the reservation event fields and keep them
   compatible with the booking producer.
2. **Idempotent consumer:** deliver the same `messageId` twice and verify the
   processed-message record prevents duplicate notification work.
3. **Kafka recovery:** test retries, dead-letter handling, consumer restarts,
   and delayed delivery while confirming notification processing cannot change
   booking inventory.
4. **AWS:** deploy after local Kafka and database behavior is understood; use a
   budget and tear down resources after practice.
5. **Load/failure drills:** pause the consumer, generate reservation events,
   restart it, and verify it catches up without repeating notifications.
