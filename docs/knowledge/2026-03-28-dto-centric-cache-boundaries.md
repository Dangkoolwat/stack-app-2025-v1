---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: en
---

# DTO-centric cache boundaries

Context
- Spring Boot 4 + Jackson 3 made Redis/Hibernate entity caching significantly more fragile in this codebase because cache payloads were coupled to Hibernate proxies, lazy relations, and API serialization rules.

Decision
- Treat Redis cache as an application DTO/read-model cache only.
- Do not use Hibernate entity graphs as Redis cache payloads.
- Keep DB entity retrieval for write paths and authorization-sensitive flows.
- Remove startup behavior that clears pre-existing caches.

Why
- Cache payloads must be serialization-stable across framework upgrades.
- DTO caches support explicit TTL, fallback, and invalidation ownership.
- Reconstructing pseudo-entities from cached DTOs is acceptable only as a temporary compatibility step and should not be the steady-state contract.

Practical rules
- Read endpoints may return DTO/read-model types directly from cache.
- Mutation flows must invalidate affected cache keys or clear dependent ranges.
- Cache failures must degrade to DB/source-of-truth reads.
- Global API serialization behavior must not be changed just to satisfy cache internals.

Verification pattern
- Add one unit test for cache hit.
- Add one unit test for cache miss + populate.
- Add one unit test for cache failure fallback.
- Add one unit test for stale-data prevention on write paths.

Operational note
- If integration tests fail before application startup due datasource bootstrapping, separate that blocker from cache-migration verification and record it explicitly instead of treating it as cache-regression evidence.
