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
