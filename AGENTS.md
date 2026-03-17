# AGENTS.md

## Purpose

This document defines the project‑level guide for AI coding agents collaborating on this repository.
The goal is to ensure that multiple agents can work together on a consistent set of rules,
traceable decisions, and a continuous history of work.  The repository is a JHipster‑based
Spring Boot backend, and this guide adapts the global agent principles to suit its specific
architecture and stack.

## Scope

The instructions in this file apply only to the `stack-app-2025-v1` repository.  They
extend the global agent rules but never weaken the requirements for accuracy,
security, verification, completeness, or grounding.  Where ambiguities exist, agents must
state assumptions explicitly and avoid guessing.  All non‑trivial work must be
recorded in the agent log as described below.

## Project Overview

This repository is a JHipster-based Spring Boot backend.

Stack includes:

- Spring Boot 4.x
- Java 21+
- Maven
- Spring Data JPA
- Liquibase
- Redis cache
- Spring Security with JWT
- RFC7807 ProblemDetail
- Swagger/OpenAPI
- JUnit5 / MockMvc

Architecture pattern:

Controller -> Service -> Domain -> Repository

Controllers should remain thin, delegating business logic to services.  Services contain
domain logic and orchestrate persistence through repositories.  Domain models should
stay free of infrastructure concerns, and repositories handle persistence only.
Avoid mixing transport (controller), business, and persistence logic in the same layer.

---

## Backend Architecture Rules

Controllers must remain thin.

Business logic belongs in the service layer.

Repositories handle persistence only.

Domain models should not contain infrastructure concerns.

Avoid mixing transport, persistence, and business logic.

---

## API Contract Rules

Backend APIs are the source of truth for:

- authentication
- authorization
- domain validation
- error contracts

Preserve API contracts unless explicitly requested.

When modifying API responses:

- document frontend impact
- maintain RFC7807 error format
- verify backward compatibility.

---

## Security Rules

High-risk areas include:

- authentication
- JWT processing
- authorization checks
- user management
- file handling
- database queries
- external integrations

Rules:

- validate input at system boundaries
- never log credentials or tokens
- maintain role-based access checks
- prefer secure defaults.


## Standard Project Documentation Structure

Project documentation lives under the repository root in a `docs/` directory.  The
recommended structure is:

```
docs/
    architecture/
    security/
    agent-log/
    decisions/
```

- architecture/ – system architecture documentation and diagrams.
- security/ – security guidelines, authentication notes, threat models, and checklists.
- agent-log/ – work history for AI agents and task continuity records.
- decisions/ – Architecture Decision Records and design trade‑offs.

Agents must preserve and update these folders when adding new documents.  New files
should be added only when genuinely necessary.
## Agent Work Log Policy

All non‑trivial work performed by AI agents must be recorded in `docs/agent-log/`.
Log files follow the naming convention `YYYY-MM-DD-task-name.md` (for example
`2026-03-09-auth-refactor.md`).  Before starting work, read the most recent
relevant log entries.  After completing a task, create a new log entry with
sufficient context so another agent can continue without rediscovery.  Reference
previous logs when building on earlier work.

## Agent Work Log Entry Requirements

Every log entry should include:

- Date – in `YYYY-MM-DD` format.
- Agent – the name or identifier of the AI system or contributor (e.g. ChatGPT, Codex).
- Task Title – a short descriptive title of the task.
- Goal – what problem is being solved and the desired outcome.
- Context – system state before the change, including related features, dependencies,
  architectural constraints, and links to relevant previous logs.
- Work Performed – a concise list of actions taken to complete the task.
- Files Modified – the list of files that were changed.
- Architecture Impact – whether the change affects project architecture; if none, state
  “No architectural changes.”
- Security Impact – describe any security implications; if none, state “No security impact.”
- Verification – how the change was verified (tests run, build commands, manual checks).
  If verification could not be performed, explain why.
- Risks – remaining risks or uncertainties.
- Next Suggested Tasks – logical follow‑up tasks for future work.
- Notes for Future Agents – assumptions, limitations, or important design decisions to
  help the next agent continue safely.

An example template is available in `docs/agent-log/template.md`.

## Engineering Principles

Follow the existing repository architecture and conventions.  Prefer the smallest
verifiable change that solves the problem.  Avoid unnecessary refactoring or
dependency changes unless explicitly requested.  Preserve compatibility unless
breaking changes are clearly required.  When requirements are incomplete, state
assumptions explicitly rather than filling gaps silently.  Favor maintainability
and clarity over cleverness.  Do not introduce cross‑layer coupling; respect
module boundaries and public contracts.

## Architecture Awareness

Before modifying code, understand the repository structure, identify the
architectural layers, and check integration points.  Remember the layering
pattern (Controller → Service → Domain → Repository).  Controllers must remain
thin; business logic belongs in the service layer.  Repositories handle
persistence only, and domain models should not contain infrastructure concerns.
Avoid mixing transport, persistence, and business logic.  Preserve public API
contracts unless the task explicitly requires a change.

## API Contract Rules

The backend API is the source of truth for authentication, authorization,
domain validation, and error contracts.  Preserve these API contracts unless
explicitly requested to modify them.  When changing API responses:

- Document the impact on the frontend.
- Maintain the RFC7807 error format.
- Verify backward compatibility, or clearly state when a breaking change is
  unavoidable.

## Security Awareness

Treat the following areas as high risk: authentication, JWT processing,
authorization checks, user management, file handling, database queries, and
external integrations.  Validate untrusted input at system boundaries, never
log credentials or tokens, maintain role‑based access checks, and prefer
secure defaults.  Clearly state security impacts when changes affect these
areas.

## Persistence Rules

Changes to database schema or persistence logic require careful review.
Liquibase changes must include the migration intent, address backward
compatibility, and describe rollback strategies.  Review entity relationships
and fetch strategies before modifying JPA mappings.  Explain cache implications
when touching Redis or Hibernate caching.

## Verification Strategy

Every meaningful change must include verification.  Preferred verification
order:

1. Targeted tests (unit tests)
2. Broader tests (integration tests)
3. MockMvc API tests
4. Maven test lifecycle (`./mvnw test` and `./mvnw verify`)
5. Build verification (`./mvnw clean package`)

If verification cannot run, clearly state what could not be verified and why.
Never mark a task complete if required verification is missing.

## Engineering Discussion Rules

When proposing architectural or structural changes, follow this format:

- Problem – describe the limitation in the current structure.
- Proposal – explain the recommended solution.
- Alternatives – provide other possible designs.
- Trade‑offs – discuss impacts on transaction boundaries, domain layering,
  API contracts, persistence model, caching, and operational complexity.
- Risks – identify migration or compatibility risks.
- Decision Needed – clearly indicate when human confirmation is required
  before applying database, contract, or security‑sensitive changes.

## Frontend Coordination

Backend changes may affect the frontend.  When modifying the authentication
flow, API contracts, error structures, or authorization rules, explicitly
state the frontend impact.  Frontend architecture details are defined in
`vue3.md`.  Coordinate with frontend agents to ensure alignment.

## Completion Requirements

A task is complete only when all requested deliverables exist, verification has
been performed, documentation is updated when needed, and an agent log entry
has been created for non‑trivial work.  Before finalizing any task, use
the following checklist:

- Architecture layering preserved.
- Security impact reviewed.
- API contracts validated.
- Persistence effects checked.
- Verification executed.
- Documentation and agent log updated.

## Final Response Structure

When completing work, the final response should briefly state:

- What was changed – a summary of the changes and rationale.
- Files modified – list of files that were created or updated.
- Verification performed – how changes were tested or validated.
- Remaining risks or assumptions – any outstanding issues or assumptions.
- Agent log entry created – confirmation that a log entry has been recorded.

Following this structure ensures transparency and continuity for the next agent.
