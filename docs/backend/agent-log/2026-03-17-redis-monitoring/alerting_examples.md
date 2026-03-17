# Redis Monitoring Alerting Examples (Prometheus/Grafana)

This document provides examples of Prometheus queries and Grafana alert rules for the metrics exposed by our application.

## 1. Prometheus Queries

### Current Memory Usage (Bytes)
```promql
redis_server_used_memory_bytes{application="stack"}
```

### Memory Usage Trend (Rate of increase)
```promql
rate(redis_server_used_memory_bytes[5m])
```

## 2. Alert Generation (Alertmanager / Grafana)

### [Alert] Redis High Memory Usage
Trigger an alert when Redis memory usage exceeds a certain threshold (e.g., 80% of maxmemory if known, or a fixed limit).
```yaml
alert: RedisHighMemoryUsage
expr: redis_server_used_memory_bytes > 8589934592 # 8GB threshold example
for: 5m
labels:
  severity: critical
annotations:
  summary: Redis memory usage is high
  description: "Redis server is using more than 8GB of memory (current: {{ $value }} bytes)."
```

### [Alert] Redis Down
Trigger an alert when the custom health indicator reported a DOWN state (if integration with Actuator Promethus metrics exists).
Note: By default, Spring Actuator Health is not always a time-series metric unless mapped. You can use the `up` metric or a custom one.

Using Prometheus `up` metric:
```promql
up{job="stack-app"} == 0
```

## 3. Grafana Dashboard Tips
- **Gauges**: Use a Gauge panel for `redis_server_used_memory_bytes`.
- **Time Series**: Use a Time Series panel to track memory usage over time to identify leaks or spikes.
- **Thresholds**: Set color thresholds (Green < 70%, Yellow 70-85%, Red > 85%).
