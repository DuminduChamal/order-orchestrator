# Order Orchestrator

An event-driven order processing system, built module by module to refresh hands-on
skills in **Java, Spring Boot, Kafka, and AWS**. It simulates the order lifecycle for an
e-commerce platform, coordinated through Kafka using a SAGA-style orchestration pattern
across independently deployable Spring Boot services.

## Where this is going

```
                     ┌───────────────────┐
   HTTP  ─────────▶  │   order-service    │──────▶  order-events (Kafka topic)
                     └───────────────────┘                │
                                                            ▼
                     ┌────────────────────┐   consumes  ┌────────────────────┐
                     │ orchestrator-service │◀───────────│  inventory-service  │
                     │  (drives the saga)   │───────────▶│  payment-service    │
                     └────────────────────┘   commands  └────────────────────┘
                                │
                                ▼
                     notification-service (consumes final state, logs/notifies)
```

| Phase | What it adds | Status |
|---|---|---|
| 1 | `order-service`: REST API, Postgres persistence, publishes `OrderCreated` to Kafka | ✅ done (this drop) |
| 1b | Integration tests for order-service with Testcontainers (Postgres + Kafka) | next up |
| 2 | `inventory-service`: consumes `OrderCreated`, reserves/rejects stock, publishes result | planned |
| 3 | `payment-service`: consumes inventory result, processes payment, publishes result | planned |
| 4 | `orchestrator-service`: central saga coordinator + compensating transactions on failure | planned |
| 5 | `notification-service`: consumes terminal order state | planned |
| 6 | Observability: Micrometer + DataDog, structured logging, tracing across services | planned |
| 7 | GraphQL gateway in front of the services (query side) | planned |
| 8 | AWS: containerize + deploy (ECS Fargate vs. MSK vs. LocalStack - TBD together) | planned |

We're deliberately building **one working slice at a time** instead of scaffolding
every service up front - each phase should run, and be worth understanding, before the
next one lands on top of it.

## Tech stack (and why these versions)

- **Java 25 (LTS)** - current recommended LTS as of 2026; Spring Boot 4 supports it fully.
- **Spring Boot 4.0 / Spring Framework 7** - GA'd November 2025. Worth knowing deliberately:
  it moved the baseline to Jakarta EE 11, adopted JSpecify null-safety annotations and
  Jackson 3, and fully modularized the Boot codebase.
- **Kafka 4.x in KRaft mode** - no ZooKeeper. ZooKeeper mode was removed entirely in Kafka
  4.0, so KRaft single-node is the realistic way to run Kafka locally now.
- **PostgreSQL 17**, **Flyway** for schema migrations (never `ddl-auto: update`).

## Running phase 1

**1. Start the infrastructure**

```bash
docker compose up -d
```

This brings up Postgres (`localhost:5432`), Kafka (`localhost:9092`), and a Kafka UI at
[localhost:8085](http://localhost:8085) for browsing topics/messages (optional - drop the
`kafka-ui` service from `docker-compose.yml` if you'd rather use the CLI).

**2. Run order-service**

```bash
cd order-service
mvn spring-boot:run
```

The app runs on `localhost:8081`. Flyway applies the migration in
`src/main/resources/db/migration` automatically on startup.

**3. Create an order**

```bash
curl -X POST localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "cust-123",
        "items": [
          { "productId": "sku-widget", "quantity": 2, "unitPrice": 19.99 },
          { "productId": "sku-gadget", "quantity": 1, "unitPrice": 49.50 }
        ]
      }'
```

You should get back a `201 Created` with the order, including a generated `id` and
`status: CREATED`. Fetch it again with:

```bash
curl localhost:8081/api/orders/{id}
```

**4. Confirm the Kafka side**

Either open the Kafka UI at `localhost:8085` and look at the `order-events` topic, or
from the CLI:

```bash
docker exec -it order-orchestrator-kafka \
  kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic order-events --from-beginning --property print.key=true
```

You should see the `OrderCreated` event, keyed by the order id (so every event for one
order lands on the same partition, in order - that matters once multiple services are
consuming this topic).

## A known gap, on purpose

`OrderService.createOrder` saves to Postgres and publishes to Kafka in the same method,
outside of any two-phase guarantee - if the Kafka send fails after the DB commit, they
drift out of sync. That's the transactional outbox pattern's problem to solve, and it's
a much better lesson once there's a second service actually depending on that event
arriving reliably. Flagging it now so it doesn't get missed later.
