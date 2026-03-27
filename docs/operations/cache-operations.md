---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: en
---

# Cache Operations Guide

## Purpose

Describe how to operate, validate, and troubleshoot the DTO-centric Redis cache used by this project.

## Operating Model

- Redis stores application DTO/read-model payloads only
- Database remains the source of truth for write flows
- Cache failures must fall back to the database or the authoritative service path

## When to Flush Redis

Flush Redis only when a cache payload contract changes in a way that is not backward-compatible.

Typical examples:

- DTO field renamed or removed
- cached key format changed without versioned coexistence
- serializer/codec behavior changed and old values can no longer be deserialized

Preferred options, in order:

1. version the cache key or cache name
2. deploy compatible readers and writers first
3. flush Redis only when compatibility cannot be maintained

## Startup Expectations

- normal application startup MUST NOT clear existing caches
- warm-up jobs may populate known read caches after startup
- warm-up failures must not block application startup

## Validation Checklist After Deployment

- confirm key cache endpoints return correct data on first request
- confirm repeated requests reduce DB access or response latency
- confirm update/delete flows invalidate affected cache keys
- confirm fallback still works when cache read/write fails

## Recommended Checks

### Read-path check

1. call a cache-backed read endpoint once
2. call the same endpoint again
3. compare response time and DB query count

### Invalidation check

1. call a cache-backed read endpoint
2. perform a write that changes the underlying data
3. call the read endpoint again
4. confirm the updated response is returned

### Fallback check

1. simulate Redis failure in a test or lower environment
2. call a cache-backed read path
3. confirm the request still succeeds through DB fallback

## Observability Signals

Track the following where available:

- cache hit ratio by cache name
- cache read/write/eviction failures
- DB query count for repeated read endpoints
- response latency for cold and warm paths
- stale-data incidents after writes

## Incident Notes

If stale data is observed:

1. identify the cache name and key
2. identify the write path that should have invalidated it
3. confirm whether the cache payload includes fields that change too frequently
4. reduce TTL or refine invalidation scope

If deserialization failures are observed:

1. confirm whether the cache payload contract changed
2. confirm whether the key or cache name was versioned
3. flush affected entries only if compatibility cannot be restored

## Related Documents

- `docs/standards/cache-safety-guideline.md`
- `docs/operations/testing-guideline.md`
- `docs/knowledge/2026-03-28-dto-centric-cache-boundaries.md`
