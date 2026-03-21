# Cache Safety Guideline

## Purpose
Define safe and predictable caching rules to prevent data inconsistency, security issues, and hidden state bugs.

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

- No entity caching for auth-related objects
- No session-like authentication state caching unless a dedicated and approved design document defines it
