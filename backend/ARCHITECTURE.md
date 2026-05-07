# High Concurrency Upgrade Notes

This backend has been upgraded from a demo implementation to a production-oriented baseline, but a real 100k-concurrency system still requires distributed deployment.

## Implemented in code

- Snowflake-style IDs to remove `AUTO_INCREMENT` and `max(id)` hotspots
- in-memory menu cache with admin-triggered invalidation
- JDBC batch insert configuration and batched order-item persistence
- semaphore-based admission control to protect MySQL during bursts
- production profile with Tomcat, Hikari, Actuator, and Prometheus support
- indexes for hot query paths

## Required next steps for true 100k concurrency

1. Deploy multiple stateless application instances behind Nginx or SLB.
2. Move hot menu data to Redis and broadcast cache invalidation.
3. Put order creation behind Kafka or RocketMQ for async peak shaving.
4. Add idempotency keys for order submission.
5. Use MySQL read/write separation and table partitioning or sharding.
6. Add gateway rate limiting, circuit breaking, and full observability.

## Production startup

```bash
java -jar ordering-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Monitoring endpoints

- `/actuator/health`
- `/actuator/prometheus`
