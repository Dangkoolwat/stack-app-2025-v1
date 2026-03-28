# Backend Engineering Guideline

Document role: reference-only implementation notes for backend contributors.
This file summarizes recurring backend patterns, but `AGENTS.md`, `docs/standards/`, `docs/workflow/`, and `docs/operations/` remain the authoritative rule sources.

## 1. Plan-First Approach
Use the repository tiered process in `AGENTS.md` for backend changes, especially for database, configuration, cache, security, or API contract work.

### Step 1: Initial Discussion
- Analyze the requirement and current system limits.
- Clarify scope before broad implementation.

### Step 2: Proposal and Trade-Off Review
- Write down the proposed design when the change is non-trivial.
- Review performance, security, maintainability, and rollback implications.

### Step 3: Implementation and Verification
- Implement according to the approved direction.
- Review query shape and index usage for Oracle-backed code.
- Run unit tests and integration tests that match the task scope.

### Step 4: Completion and Documentation
- Record the outcome in the required agent log.
- Capture follow-up debt or reusable lessons in `docs/knowledge/` when needed.

## 2. Explicit Bans
- Do not propose schema changes outside Liquibase.
- Do not return or receive entities directly at the resource layer unless an authoritative guide explicitly allows it.
- Do not use Oracle reserved words directly as physical table or column names.
- Do not perform long-running work inside a single transaction.

## 3. Database Notes
- Oracle treats empty strings as `NULL`, so write defensively for compatibility.
- Use paged queries for large result sets.
- Keep timezone handling explicit and consistent across server, application, and database.

## 4. Review Checklist
- [ ] Was the change scoped and proposed at the right tier?
- [ ] Do Liquibase changes respect Oracle syntax and identifier constraints?
- [ ] Do API responses use DTOs and the standard error format?
- [ ] Were required documentation artifacts updated?

## 5. JSON and Cache Change Notes

### 5.1 Jackson Changes

When changing Jackson behavior:

- Check the dependency tree.
- Remove mixed Jackson stacks.
- Prefer the repository-standard mapper setup over ad-hoc mapper creation.
- Verify OpenAPI and JSON serialization paths after the change.

### 5.2 Cache Changes

For cache changes, follow the DTO-centric policy from `docs/standards/cache-safety-guideline.md`.

Key reminders:
- Classify whether the target is an entity, a read model, or auth-adjacent state.
- Never cache a JPA entity or Hibernate proxy directly.
- Do not mix Hibernate L2 assumptions with application JSON cache behavior.
- Define DTO shape, TTL, invalidation owner, and fallback behavior before implementation.
- Keep auth-adjacent infrastructure caches explicit and narrow, such as OTP, rate limiting, or `UserAuthCacheDto`.

---

### 5.3 Redis Changes

Required checks:
- Verify connection growth is justified.
- Avoid duplicate `RedissonClient` creation paths.

Do not:
- Create Redis clients per service.
- Expand Redis connections per feature without centralized ownership.

---

### 5.4 Additional Review Checklist

- [ ] Is there a single Jackson strategy in use?
- [ ] Are JPA entities excluded from Redis payloads?
- [ ] Does each cache DTO use simple fields only?
- [ ] Does DTO conversion happen inside the transaction boundary?
- [ ] Is eviction wired to every state-changing path?
- [ ] Does cache failure fall back safely to DB or source-of-truth logic?
- [ ] Is Redis client ownership centralized?
- [ ] Does TTL match the data volatility?
