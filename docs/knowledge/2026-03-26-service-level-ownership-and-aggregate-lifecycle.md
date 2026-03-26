---
agent: GPT-5.4
created_at: 2026-03-26 (Thu)
language: en
---

# Service-level ownership and aggregate lifecycle

Context
- A refactoring pass introduced centralized authorization and aggregate deletion behavior, but several critical guarantees still depended on request payloads or partial lifecycle handling.

What happened
- Board and comment creation still trusted `userId` from incoming DTOs.
- Private upload download only checked authentication and missed owner/admin authorization.
- Board aggregate restore and hard delete did not fully reconcile descendants and tag usage counts.

Decision
- Enforce creator ownership inside service methods, not only at controller boundaries.
- Route private upload download through a dedicated service method that validates existence, visibility, deletion state, and owner/admin access.
- Treat board restore and hard delete as aggregate operations that must update comments, uploads, board-tag relations, and tag usage counts together.

Why it matters
- Request payloads are not a trust boundary.
- Partial aggregate lifecycle handling creates long-lived data drift, especially around counters and soft-deleted descendants.
- Integration tests should include hostile cross-user scenarios, not only happy paths.

Verification pattern
- Add one test that attempts identity spoofing on create.
- Add one test that verifies another authenticated user is rejected from a private resource.
- Add one aggregate-level admin test for restore and hard delete side effects.

Recommended reuse
- Any feature that accepts ownership-related identifiers should resolve the actor from `SecurityUtils` in the service layer.
- Any aggregate delete/restore flow should explicitly define descendant scope, counter updates, and cache invalidation as one transaction boundary.
