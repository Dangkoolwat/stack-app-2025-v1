# Cache Safety Guideline

## Purpose

Define safe and predictable caching rules to prevent data inconsistency, security issues, and hidden state bugs.

This project uses a DTO-centric application cache policy.
Redis cache entries are treated as application read models, not as Hibernate entity state.

---

## Rule Interpretation

- MUST = mandatory
- SHOULD = recommended default
- MAY = optional

---

## Forbidden (MUST NOT)

- MUST NOT cache JPA `@Entity` objects
- MUST NOT cache Hibernate proxies, lazy-loaded collections, or persistence-managed object graphs
- MUST NOT rely on Hibernate second-level cache or query cache for Redis-backed application caching unless a dedicated exception is approved
- MUST NOT cache authentication-related data such as `UserDetails`, authorities, or session-like state
- MUST NOT cache data that contains sensitive information unless a separate approved policy explicitly allows it
- MUST NOT change global API serialization rules only to satisfy cache serialization needs
- MUST NOT reconstruct cached DTOs into pseudo-entities as the steady-state service contract

---

## Allowed (Strict Conditions)

- Only DTO-based caching is allowed
- Cached values MUST be explicit DTOs or read models with a stable serialization contract
- Cached DTOs SHOULD be immutable
- Cached DTOs MUST NOT contain lazy-loaded references or entity proxies
- Redis MAY be used for OTP, rate limiting, locking, and similarly bounded operational use cases

### DTO Immutability Clarification

For this guideline, a DTO is treated as immutable when all of the following are true:

- it does not expose business-state setter methods after creation
- it does not hold lazy-loaded references or entity proxies
- it is safe to serialize and read without mutating shared state

### Cache Contract Requirement

Every new cache entry MUST define the following before implementation:

- payload type
- key format
- TTL
- invalidation owner
- fallback source-of-truth path

Recommended example:

```text
cache: common_details_by_group
payload: List<CommonCodeDetailReadModel>
key: group:{groupCode}:v1
ttl: 86400s
owner: CommonCodeService
fallback: CommonCodeDetailRepository.findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc
```

---

## Expiration Rules

- All cache entries MUST define TTL
- Default TTL SHOULD be short unless a longer TTL is explicitly justified
- Long-lived caches MUST document the reason and invalidation strategy
- Cache bootstrap MUST NOT clear existing entries on normal application startup

---

## Invalidation Rules

- Cache MUST be evicted when the underlying state changes
- Write operations MUST trigger eviction for affected keys or ranges
- Bulk updates MUST consider broader invalidation impact

### State Change Examples

State changes include:

- create, update, delete operations
- permission or status changes
- configuration changes that alter cached output
- batch jobs that rewrite cached source data

---

## Key Design Rules

- Cache keys MUST be deterministic
- Cache keys SHOULD include the resource identifier
- Cache keys SHOULD include version or context when contract changes are possible
- Cache keys SHOULD be versioned when payload shape changes can coexist during rolling deployment

Example:

```text
user-profile:{userId}
product:{productId}:v1
```

---

## Fallback Rule

- The system MUST support DB or source-of-truth fallback
- Cache failure MUST NOT break core functionality
- Cache read, write, and eviction failures SHOULD degrade gracefully with logging

---

## Clarification

"Do not cache authentication data" means:

- no entity caching for auth-related objects
- no session-like authentication state caching unless a dedicated and approved design document defines it

Approved project pattern:

- dedicated DTO cache for authentication support data
- short TTL
- explicit eviction on state change
- DB fallback when Redis is unavailable

---

## Project Policy

For this repository, the default cache strategy is:

- Redis application cache stores DTO/read-model payloads only
- write flows continue to use DB-managed entities as source of truth
- read flows MAY return DTO/read-model responses directly from cache
- Hibernate entity caching is disabled by default unless a future exception is explicitly approved and benchmarked
