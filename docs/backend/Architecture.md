# Backend Architecture Notes

Document role: reference-only architecture notes for backend contributors.
This file helps readers understand the intended backend shape, but it does not override `AGENTS.md`, `docs/standards/`, `docs/workflow/`, or `docs/operations/`.

## Core Principles
- Maintainability First
- Security by Default
- Clear Layer Separation

---

## Recommended Backend Patterns

### Controller
- Request/Response DTO mapping only
- No business logic

### Service
- Business logic (use-case oriented)
- Transaction boundary

### Repository
- Data access only
- Clear query intent

---

## Maintainability Guidelines

Prefer:
- Explicit queries over magic
- Small services with clear responsibility
- Short transactions

Avoid:
- Deep abstraction layers
- Hidden side effects
- Over-generalization

---

## Security Defaults

- Validate all inputs.
- Use the standard RFC7807 problem format.
- Never log secrets or tokens.
- Do not cache JPA entities directly in Redis.
- Cache only cache-specific DTOs or read models.
- Do not cache `UserDetails` or similar security proxy types with `@Cacheable`.

---

## Change Playbook

### 1. Config / Library Change

Must define:
- Why change is needed
- Scope of impact
- Compatibility and possible breaking behavior
- Rollback strategy
- Required tests

Checklist:
- [ ] Dependency tree checked
- [ ] No duplicate libraries (e.g. Jackson)
- [ ] Configuration consistency maintained
- [ ] Runtime impact verified

---

### 2. API Development

Steps:
1. Define contract (DTO)
2. Validate inputs
3. Implement service logic
4. Standardize error handling
5. Document (OpenAPI)

---

### 3. Database Change

- Use Liquibase only
- Never modify schema manually
- Ensure backward compatibility
- Plan rollback

---

## Caching Notes (Redis)

Use ONLY when:
- High read frequency
- Low consistency risk

### Cache Target Selection

Allowed:
- Cache-specific DTOs or read models with simple fields only.
- OTP codes, failure counts, and account lock status when TTL is explicit.
- Rate limiting state and distributed lock state through dedicated Redis primitives.

Forbidden:
- Direct caching of JPA entities such as `User`, `Authority`, or `Board`.
- `@Cacheable` usage for `UserDetails`, security authorities, or proxy-backed auth objects.
- Mixing Hibernate L2 cache assumptions with application JSON cache assumptions.

### Cache Design Rules

- TTL is mandatory and must match data volatility.
- Manage cache only in the service layer.
- Evict explicitly on state changes.
- Guarantee DB fallback when Redis is unavailable.
- Convert to DTOs inside the transaction boundary before caching.

---

## Preferred Defaults

- JSON: Single ObjectMapper
- DB: Oracle + Liquibase
- Cache: Redis centralized
- Error: RFC7807
- Auth: Stateless (JWT)

---

## Self-Check Before Merge

- [ ] Business logic is clear and testable
- [ ] No layer violation
- [ ] Security review done
- [ ] Performance acceptable
- [ ] Rollback possible

---

## Golden Rule

 "Write code someone else can safely change in 6 months."
