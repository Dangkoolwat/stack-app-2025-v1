---
agent: GPT-5.4
created_at: 2026-03-25 (수)
language: en
---

# Project-Wide Review Based on the Existing Entity-Centered Analysis

## 1. Purpose

This document extends the entity-centered review in `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md` and reframes the project as a whole-system review.

The main goal is not to repeat every board-domain issue already captured there, but to identify what is still missing at the project level:

- authorization boundaries
- operational safety
- cache consistency
- test execution reliability
- documentation governance

## 2. Review Basis

Reviewed sources:

- `AGENTS.md`
- `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md`
- `docs/analysis/2026-03-25-antigravity/resource-management-plan.md`
- `docs/standards/environment-variables-guideline.md`
- `docs/standards/cache-safety-guideline.md`
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java`
- `src/main/java/com/daangcool/stack/service/board/BoardService.java`
- `src/main/java/com/daangcool/stack/service/board/CommentService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/service/board/BoardTagService.java`
- `src/main/java/com/daangcool/stack/web/rest/BoardResource.java`
- `src/main/java/com/daangcool/stack/web/rest/CommentResource.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`
- `pom.xml`
- representative tests under `src/test/java`

## 3. Executive Summary

The project has strong intent and a meaningful amount of supporting infrastructure:

- layered backend structure
- explicit cache configuration
- DTO-oriented service boundaries in many places
- broad integration-test presence
- solid initial entity analysis already written by another agent

However, the current project still has several system-level gaps that are more dangerous than typical implementation bugs:

1. Management endpoints appear to be publicly exposed because the security matcher order is unsafe.
2. Object-level authorization is weak across board, comment, and upload flows.
3. The existing board-domain integrity issues remain important and should be treated as platform risk, not as isolated board bugs.
4. Cache invalidation rules are inconsistent across services.
5. Public file delivery still loads full payloads into memory.
6. A non-trivial portion of unit tests is likely not executed by default because of test naming.
7. Documentation governance is drifting away from the repository policy.

The highest priority is security hardening first, then integrity/caching, then test and documentation reliability.

## 4. What the Existing Antigravity Analysis Already Covers Well

The existing review is valuable and should remain the baseline for board-domain integrity work.

It already identifies important issues such as:

- soft-delete and unique-constraint collision in tag reuse
- tag usage count drift
- missing soft-delete cascade for board children
- missing restore cascade
- potential hard-delete foreign key failure
- service responsibility leakage between `BoardService` and `BoardTagService`

That document is strong on entity relationships and lifecycle consistency. The additional findings below are the project-wide gaps that were still underexplored.

## 5. Additional Findings Missing From the Current Analysis

### Finding A. Management endpoints are likely public because the security matcher order is unsafe

Severity: Critical

Evidence:

- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java:136` defines `"/management/**"` as a public path.
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java:121` permits `applicationProperties.getSecurity().getPublicPaths().getManagement()`.
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java:122-123` then tries to restrict `/management/prometheus` and `/management/**` to admin.

Risk:

- In Spring Security request matching, earlier matching rules take precedence.
- That means the broader `permitAll()` rule for `/management/**` can make the later admin-only rules ineffective.
- If that behavior is active at runtime, operational endpoints such as health, metrics, env-like surfaces, and other management handlers may be accessible without admin authorization.

Recommended solution:

1. Remove `/management/**` from `application.security.public-paths.management` defaults.
2. Explicitly allow only the exact public endpoints you truly want, for example `/management/health` and optionally `/management/health/**`.
3. Keep all remaining `/management/**` endpoints admin-only.
4. Add integration tests that verify:
   - anonymous access allowed only for intended health endpoints
   - anonymous access forbidden for `/management/info`, `/management/metrics`, `/management/prometheus`, and other management routes

Alternative:

- Keep `publicPaths.management`, but restrict it to a minimal allowlist rather than a wildcard.

### Finding B. Object-level authorization is missing across board, comment, and upload flows

Severity: Critical

Evidence:

- `src/main/java/com/daangcool/stack/service/board/BoardService.java:90-101`
  - if `userId` is provided in the request, the service trusts it instead of forcing the authenticated principal.
- `src/main/java/com/daangcool/stack/service/board/BoardService.java:165-181`
  - board update has no ownership or admin check.
- `src/main/java/com/daangcool/stack/service/board/CommentService.java:79-89`
  - comment creation trusts request `userId`.
- `src/main/java/com/daangcool/stack/service/board/CommentService.java:161-190`
  - comment update and delete have no ownership or admin check.
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:87-91`
  - delete endpoint requires only authentication and delegates directly.
- `src/main/java/com/daangcool/stack/service/board/UploadService.java:121-130`
  - upload soft delete performs no owner/admin authorization.
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:203-240`
  - private file download requires authentication, but there is no per-file ownership or entitlement verification before streaming.

Risk:

- Any authenticated user may be able to:
  - create boards/comments as another user by sending a different `userId`
  - update or delete another user’s board/comment
  - soft-delete another user’s upload
  - download private files that do not belong to them

This is a broken object-level authorization pattern, not a single endpoint bug.

Recommended solution:

1. Introduce a shared authorization policy layer for ownership checks.
2. For user-facing create APIs, ignore incoming `userId` and always derive the owner from `SecurityUtils.getCurrentUserLogin()`.
3. For update/delete/download operations, enforce:
   - owner access
   - admin override
4. Add negative integration tests for cross-user access on:
   - board update/delete
   - comment create/update/delete
   - upload delete
   - private upload download

Preferred implementation direction:

- Centralize authorization in dedicated helper methods or a domain-specific authorization service instead of repeating ad hoc checks in every controller.

### Finding C. Board lifecycle integrity is still a project-level risk, not only a board-module issue

Severity: High

Baseline:

- `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md:49-120` and later sections already describe the core board-domain integrity issues.

Why this is still a project-wide finding:

- `Board`, `BoardTag`, `Upload`, and `Comment` together form one user-visible content aggregate.
- Current behavior leaves soft-delete, restore, and hard-delete flows only partially modeled.
- That affects more than board correctness:
  - cache correctness
  - orphan-resource detection
  - admin cleanup safety
  - tag analytics correctness
  - data retention behavior

Recommended solution:

1. Promote board lifecycle handling to an aggregate policy.
2. Define explicit rules for:
   - soft delete
   - restore
   - hard delete
3. Treat the board aggregate as:
   - `Board`
   - `BoardTag`
   - `Upload`
   - `Comment`
4. Make cleanup order explicit and test it.
5. Add one integration test per lifecycle:
   - board soft delete cascades
   - board restore cascades
   - board hard delete removes children safely

### Finding D. Cache invalidation contracts are inconsistent across services

Severity: High

Evidence:

- `src/main/java/com/daangcool/stack/service/board/BoardService.java:54-59` uses `CACHE_BOARD_PAGE = "BOARD_PAGE_V2"`.
- `src/main/java/com/daangcool/stack/service/board/BoardTagService.java:46-49` still uses `CACHE_BOARD_PAGE = "BOARD_PAGE"`.
- `src/main/java/com/daangcool/stack/service/board/BoardTagService.java:82-85` clears the older cache name.

Risk:

- When board-tag relationships change through `BoardTagService`, page/search cache invalidation can miss the actual board page cache.
- That produces stale board listing behavior and undermines cache safety rules in `docs/standards/cache-safety-guideline.md`.

Recommended solution:

1. Move cache names into a single shared cache-name contract.
2. Replace duplicated string constants across services.
3. Add one cache invalidation test proving that board listings are refreshed after tag changes.

Preferred implementation direction:

- A dedicated `CacheNames` class or package-level constants for each bounded context.

### Finding E. Public file delivery still loads the entire payload into memory

Severity: Medium

Evidence:

- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:104-131`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java:154-182`

Risk:

- `readAllBytes()` can cause high memory pressure or OOM under large file downloads or concurrent traffic.
- The class-level documentation already states that streaming should be preferred.
- The private download endpoint already uses `StreamingResponseBody`, so the public endpoints are inconsistent with the intended design.

Recommended solution:

1. Change public download and preview endpoints to stream rather than buffer the full file.
2. Keep content type and content disposition headers, but move the payload path to `StreamingResponseBody`.
3. Add at least one large-file test or mock-based streaming behavior test.

### Finding F. Many unit tests are likely not executed by default

Severity: High

Evidence:

- `pom.xml:673-685` configures Surefire exclusions but does not add custom includes for `*T.java`.
- The repository contains multiple tests named with the `T.java` suffix, for example:
  - `src/test/java/com/daangcool/stack/service/board/BoardServiceT.java`
  - `src/test/java/com/daangcool/stack/service/board/UploadServiceT.java`
  - `src/test/java/com/daangcool/stack/service/board/CommentServiceT.java`
  - `src/test/java/com/daangcool/stack/service/storage/LocalDefaultFileStorageServiceT.java`

Inference:

- Under standard Surefire defaults, `*T.java` classes are typically not part of the default include pattern.
- If that default is active here, a meaningful portion of service-level unit tests is not running in normal `test` builds.

Risk:

- The project may appear well-tested while silently skipping important unit tests.
- This is especially dangerous because several security and lifecycle gaps exist exactly in the service layer.

Recommended solution:

1. Standardize all unit test names to `*Test.java`.
2. Or explicitly configure Surefire includes for `**/*T.java`.
3. Add CI reporting that shows executed test counts by class.
4. Prefer renaming over custom includes because it lowers cognitive overhead for future contributors.

### Finding G. Documentation governance is drifting from the repository policy

Severity: Medium

Evidence:

- `AGENTS.md:5` requires all shared documentation under `docs/` to be written in English.
- `docs/analysis/2026-03-25-antigravity/board-entity-relationship-analysis.md:4` declares `language: ko`.
- The body of that same analysis is primarily written in Korean.
- `AGENTS.md:24-31` defines the required top-level documentation structure, while the repository also contains `docs/release-notes`.

Risk:

- Review outputs become inconsistent across agents and over time.
- Shared engineering knowledge becomes less searchable and less reusable.
- Process rules lose authority when exceptions accumulate without explicit approval.

Recommended solution:

1. Treat English as mandatory for all future shared docs under `docs/`.
2. Migrate key cross-team documents to English in priority order:
   - analysis
   - operations
   - standards-adjacent supporting docs
3. Decide whether `docs/release-notes` is an approved extension or should be relocated/documented in policy.

## 6. Alternative Solutions and Decision Guide

This section converts the major findings into implementation options so the next engineering step can be chosen intentionally rather than reactively.

### A. Management endpoint exposure

Recommended option:

- Remove the wildcard public configuration for `/management/**` and expose only explicit health endpoints.

Why this is the best fit:

- It is the safest operational default.
- It makes intent obvious in both configuration and code review.
- It minimizes future regression risk when new management endpoints are added.

Alternative 1:

- Keep a public-path configuration property, but change its default value to a minimal allowlist such as `/management/health` and `/management/health/**`.

Pros:

- Keeps configurability.
- Works well when different environments need different public health visibility.

Cons:

- Still depends on disciplined configuration management.
- Easier to reintroduce unsafe wildcard values later.

Alternative 2:

- Separate management exposure by profile, exposing public health only in specific deployment profiles.

Pros:

- Stronger environment control.
- Good for production systems with different infra requirements.

Cons:

- More complex to reason about.
- Higher chance of profile drift between local, CI, and production.

Decision rule:

- If the project wants the simplest and safest path, use the recommended option.
- If environment-specific actuator visibility is a real business requirement, use Alternative 1 plus integration tests.

### B. Object-level authorization gaps

Recommended option:

- Introduce a shared authorization service or policy layer that evaluates ownership and admin override centrally.

Why this is the best fit:

- The same vulnerability pattern appears in boards, comments, and uploads.
- Centralization reduces duplicated logic and inconsistent fixes.
- It is easier to test and audit.

Alternative 1:

- Add ownership checks directly inside each service method.

Pros:

- Fastest short-term patch.
- Minimal refactoring cost.

Cons:

- Logic becomes duplicated.
- Future endpoints are likely to miss the same checks again.

Alternative 2:

- Move most ownership checks to method security with custom `@PreAuthorize` expressions.

Pros:

- Strong declarative style.
- Easy to spot authorization rules at entry points.

Cons:

- Can become hard to debug when rules need repository lookups.
- Often still requires service-side validation for non-controller call paths.

Decision rule:

- For emergency patching, Alternative 1 is acceptable.
- For long-term maintainability, the recommended option is better.
- If the team already favors declarative authorization heavily, Alternative 2 can be used together with a shared checker bean.

### C. Board aggregate lifecycle integrity

Recommended option:

- Define the board aggregate explicitly and implement one orchestration path for soft delete, restore, and hard delete.

Why this is the best fit:

- The project already behaves as if `Board`, `BoardTag`, `Upload`, and `Comment` are one functional unit.
- Aggregate-level orchestration makes cache, orphan cleanup, and analytics easier to reason about.

Alternative 1:

- Keep the current service split, but add missing cascades manually in `BoardService`.

Pros:

- Smaller code delta.
- Faster to ship.

Cons:

- Keeps too much cross-domain knowledge inside one service.
- Makes future maintenance harder.

Alternative 2:

- Push more lifecycle behavior into domain events or asynchronous cleanup jobs.

Pros:

- Better decoupling.
- Can scale for heavier cleanup workloads.

Cons:

- Adds eventual consistency.
- Harder to debug and test than a synchronous transactional flow.

Decision rule:

- If correctness must be restored quickly, Alternative 1 is a practical bridge.
- If the team wants a cleaner domain model, use the recommended orchestration approach.
- Avoid Alternative 2 unless there is a clear scalability reason.

### D. Cache invalidation inconsistency

Recommended option:

- Introduce a single shared cache-name contract and remove duplicated cache string declarations from individual services.

Why this is the best fit:

- The current defect exists because names are duplicated.
- A shared contract prevents silent drift.

Alternative 1:

- Keep local constants but enforce consistency with tests.

Pros:

- Low refactoring cost.

Cons:

- Still allows drift.
- Requires developers to remember multiple sources of truth.

Alternative 2:

- Replace manual cache management with more declarative caching annotations where possible.

Pros:

- Less hand-written eviction code.
- Better consistency for straightforward CRUD flows.

Cons:

- Harder to model complex multi-entity invalidation.
- Existing explicit cache patterns are already widely used in this project.

Decision rule:

- Use the recommended option for cache names immediately.
- Consider Alternative 2 only after naming and lifecycle consistency are fixed.

### E. Public file download memory usage

Recommended option:

- Convert public download and preview endpoints to streaming responses.

Why this is the best fit:

- The private endpoint already uses streaming.
- This aligns implementation with the current class documentation.

Alternative 1:

- Keep `byte[]` responses but add file-size limits for public download endpoints.

Pros:

- Smallest change.
- Can reduce worst-case memory spikes quickly.

Cons:

- Still not robust for concurrent load.
- Adds artificial product limitations.

Alternative 2:

- Offload public file delivery to the web server, CDN, or object-storage signed URL flow.

Pros:

- Best scalability.
- Lower app-server memory and bandwidth pressure.

Cons:

- More infrastructure complexity.
- Requires stronger storage and access-control design.

Decision rule:

- If the project wants immediate safety without infra redesign, use the recommended option.
- If file traffic is expected to grow significantly, Alternative 2 should become part of the medium-term architecture.

### F. Test execution reliability

Recommended option:

- Rename `*T.java` unit tests to `*Test.java` and keep Maven defaults simple.

Why this is the best fit:

- It matches common Java ecosystem conventions.
- New contributors will understand it immediately.
- CI becomes easier to reason about.

Alternative 1:

- Keep existing names and add Surefire include rules for `**/*T.java`.

Pros:

- Minimal file rename churn.

Cons:

- Non-standard convention remains.
- Future contributors may still add tests that are not picked up.

Alternative 2:

- Split test suites more formally into unit, integration, and architecture profiles with explicit naming and plugin rules.

Pros:

- Strong long-term build discipline.
- Clearer CI stages.

Cons:

- Higher initial setup cost.
- More maintenance burden for a small or medium-sized team.

Decision rule:

- If the goal is to restore trust quickly, use the recommended option.
- If the team already plans CI restructuring, Alternative 2 can be adopted as part of that work.

### G. Documentation language and governance drift

Recommended option:

- Keep the repository policy unchanged and migrate shared cross-team documents under `docs/` to English.

Why this is the best fit:

- It matches the current project rule.
- It improves consistency for future agents and collaborators.

Alternative 1:

- Officially revise `AGENTS.md` to allow bilingual documents in specific folders such as `docs/analysis`.

Pros:

- Better local readability for Korean-speaking maintainers.

Cons:

- Weakens the current single-language governance model.
- Searchability and consistency may drop again.

Alternative 2:

- Keep shared docs in English, but require a short Korean summary section in agent logs or PR descriptions.

Pros:

- Balances repository consistency with local readability.
- Avoids changing the `docs/` language rule.

Cons:

- Requires process discipline outside the document itself.

Decision rule:

- If the project is primarily maintained in Korean but still wants disciplined shared docs, Alternative 2 is the most balanced supplement.
- If the team wants fully Korean shared docs, `AGENTS.md` must be changed explicitly first.

## 7. Recommended Remediation Roadmap

### Phase 1. Immediate security hardening

1. Fix management endpoint matcher order and public path configuration.
2. Enforce owner/admin authorization for board, comment, and upload operations.
3. Add negative integration tests for unauthorized cross-user access.

### Phase 2. Aggregate integrity and cache correctness

1. Implement board aggregate lifecycle rules for delete, restore, and hard delete.
2. Unify cache name constants and invalidation contracts.
3. Verify orphan resource logic after lifecycle fixes.

### Phase 3. Reliability and governance

1. Normalize test naming so all intended tests execute.
2. Add security and cache-focused regression tests.
3. Bring shared documentation back into policy compliance.

## 8. Suggested Verification Checklist

Before calling the project review complete, the repository should be able to prove the following:

- anonymous access to `/management/**` is restricted to the exact intended subset
- authenticated non-owners cannot edit or delete another user’s board/comment/upload
- authenticated non-owners cannot download another user’s private file
- board delete/restore/hard-delete flows preserve aggregate consistency
- tag changes invalidate the real board page cache
- all intended unit tests are executed in CI
- shared docs under `docs/` follow the language policy

## 9. Final Assessment

The project is not weak in ambition or structure. The bigger problem is that several strong-looking subsystems are not yet fully aligned with one another:

- authentication exists, but authorization is incomplete
- caching exists, but invalidation contracts are fragmented
- tests exist, but execution reliability is questionable
- analysis exists, but documentation policy is not consistently enforced

The most important next step is to treat security authorization and lifecycle integrity as cross-cutting architecture work, not as local bug fixes.
