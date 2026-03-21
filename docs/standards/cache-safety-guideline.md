# Cache Safety Guideline

## Purpose

Define safe and predictable caching rules to prevent data inconsistency, security issues, and hidden state bugs.

---

## Rule Interpretation

- MUST = mandatory
- SHOULD = recommended default
- MAY = optional

---

## Forbidden (MUST NOT)

- MUST NOT cache JPA `@Entity` objects
- MUST NOT cache authentication-related data such as `UserDetails`, authorities, or session-like state
- MUST NOT cache data that contains sensitive information unless a separate approved policy explicitly allows it

---

## Allowed (Strict Conditions)

- Only DTO-based caching is allowed
- Cached DTOs SHOULD be immutable
- Cached DTOs MUST NOT contain lazy-loaded references or entity proxies
- Redis MAY be used for OTP, rate limiting, locking, and similarly bounded operational use cases

### DTO Immutability Clarification

For this guideline, a DTO is treated as immutable when all of the following are true:

- it does not expose business-state setter methods after creation
- it does not hold lazy-loaded references or entity proxies
- it is safe to serialize and read without mutating shared state

---

## Expiration Rules

- All cache entries MUST define TTL
- Default TTL SHOULD be short unless a longer TTL is explicitly justified
- Long-lived caches MUST document the reason and invalidation strategy

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

Example:

```text
user-profile:{userId}
product:{productId}:v1
```

---

## Fallback Rule

- The system MUST support DB or source-of-truth fallback
- Cache failure MUST NOT break core functionality

---

## Clarification

"Do not cache authentication data" means:

- no entity caching for auth-related objects
- no session-like authentication state caching unless a dedicated and approved design document defines it
