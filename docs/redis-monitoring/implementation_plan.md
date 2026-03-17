# Redis Monitoring and Alerting Implementation Plan

This plan outlines the steps to enable monitoring and alerting for Redis (Redisson), specifically focusing on connection pool status and server capacity (memory usage).

## User Review Required

> [!NOTE]
> Monitoring and alerting are most effective when integrated with an external system like Prometheus/Grafana. This implementation will expose the data through Spring Boot Actuator endpoints.

## Proposed Changes

### [Backend Monitoring]

#### [NEW] [RedisMonitoringConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/RedisMonitoringConfiguration.java)
- Create a configuration class to register Redisson metrics and health indicators.
- **RedissonMetrics**: Connect `RedissonClient` to `MeterRegistry` to expose connection pool metrics:
    - `redisson.connection_pool.total_size`
    - `redisson.connection_pool.available_size`
    - `redisson.connection_pool.free_size`
    - `redisson.connection_pool.busy_connections`
- **Custom RedisHealthIndicator**: Extend `AbstractHealthIndicator` to include Redis `INFO` data (like `used_memory`, `maxmemory`) in the `/management/health` endpoint.

---

## Verification Plan

### Automated Tests
- Create `RedisMonitoringConfigurationTest.java` to verify that the `MeterRegistry` contains Redisson-related meters after initialization.
- **Commands**:
    ```bash
    ./mvnw test -Dtest=RedisMonitoringConfigurationTest
    ```

### Manual Verification
1.  Run the application locally: `./mvnw`
2.  Access the Prometheus metrics endpoint: `curl http://localhost:8080/management/prometheus`
3.  Verify that `redisson_connection_pool_busy_connections` (or similar name depending on naming convention) is present.
4.  Access the Health endpoint: `curl http://localhost:8080/management/health`
5.  Verify that the `redis` section contains `used_memory` and `status: UP`.
