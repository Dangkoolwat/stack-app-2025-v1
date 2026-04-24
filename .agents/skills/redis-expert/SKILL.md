---
name: redis-expert
description: Concise Redis expert guide for Spring Boot applications using Redisson and Spring Data Redis.
---

# Redis Expert (Slim)

## 1. Core Data Structures
- **Strings**: Basic key-value, used for tokens/sessions.
- **Hashes**: Object mapping, efficient for small objects.
- **Sets**: Unique collections, used for tags/permissions.
- **Sorted Sets**: Ordered unique items, used for leaderboards/priority.
- **Streams**: Event logs, used for async processing.

## 2. Spring Boot / Redisson Integration
- **Configuration**: Managed via `CacheConfiguration.java` and `application.yml`.
- **RedissonClient**: Primary bean for atomic operations, distributed locks, and reactive patterns.
- **@Cacheable**: Spring abstraction for transparent caching.
- **TTL**: Always set TTL for cache entries to avoid memory bloat.

## 3. Distributed Locking (Redisson)
```java
RLock lock = redissonClient.getLock("resource:name");
if (lock.tryLock(10, 60, TimeUnit.SECONDS)) {
    try {
        // Critical section
    } finally {
        lock.unlock();
    }
}
```

## 4. Best Practices
- **Key Naming**: Hierarchical using colons (e.g., `user:1000:profile`).
- **Data Serialization**: Prefer DTOs over raw JPA entities.
- **Monitoring**: Use `redis-cli INFO` and `SCAN` instead of `KEYS`.
- **Eviction**: Monitor memory and set `allkeys-lru` policy in production.

## 5. Prohibited Anti-Patterns
- ❌ Using `KEYS *` in production (use `SCAN`).
- ❌ Caching giant binary blobs (>1MB).
- ❌ Storing JPA entities directly without DTO conversion.
- ❌ Missing TTL for temporary cache keys.
