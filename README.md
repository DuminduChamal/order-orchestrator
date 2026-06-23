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
| 1 | `order-service`: REST API, Postgres persistence, publishes `OrderCreated` to Kafka | in progress |
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
