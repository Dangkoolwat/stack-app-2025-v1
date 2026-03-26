---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: en
---

# Spring Boot 4 test execution contract

## Context

After the Spring Boot 4 and JHipster 9 style test migration, the repository contained an inconsistency between documented test expectations and the actual Maven execution path.

`maven-surefire-plugin` excluded `*IT` classes, but repository-level guidance still suggested `./mvnw test` as the primary validation command for both unit and integration confidence.

## Decision

The repository now treats test execution as two explicit layers:

1. `./mvnw test`
   - unit tests and non-`*IT` test classes executed by Surefire
2. `./mvnw verify`
   - full validation including `*IT` and `*IntTest` classes executed by Failsafe

## Why this matters

- It prevents false-positive completion reports where integration tests were not actually run.
- It aligns agent logs with Maven report locations.
- It makes Spring Boot 4 Testcontainers-based verification reproducible.

## Additional guidance

- Security-sensitive integration tests should prefer Bearer-token-based verification using `JwtAuthenticationTestUtils`.
- Legacy `@WithMockUser` tests may remain temporarily, but modified security-sensitive integration tests should be converted first.
- Testcontainers reuse warnings are environmental, not functional failures. They should be documented separately from code correctness.

## Verification note

When reporting success, future agents should record:

- the exact Maven command used
- whether Surefire or Failsafe reports were checked
- the specific test classes or scope that were verified

