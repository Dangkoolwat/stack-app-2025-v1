---
agent: Antigravity
created_at: 2026-03-26 (Thursday)
language: en
---

# Testing Guideline Operations Guide

## Overview

This document establishes the standard procedures for performing tests in the modern Spring Boot 4 environment. It is mandatory for all agents and developers to follow these patterns to ensure security, performance, and data integrity.

---

## 1. Running Tests

Always use the `.env` file to load environment variables. This prevents configuration mismatches between local and containerized environments.

### Standard Commands (Bash/Zsh)

```bash
export $(grep -v '^#' .env | xargs) && ./mvnw test
```

```bash
export $(grep -v '^#' .env | xargs) && ./mvnw verify
```

### Command Roles

- `./mvnw test`: unit tests and non-`*IT` test classes validated by Surefire
- `./mvnw verify`: full verification including `*IT` / `*IntTest` classes via Failsafe

### Key Parameters
- `testdev`: Primary profile for local integration tests.
- `test`: Active profile for standard CI/CD verification.

---

## 2. Spring Boot 4 Standards

### Infrastructure Patterns
- Use the `@IntegrationTest` composite annotation for all integration tests.
- Leverage `spring-boot-testcontainers` with `@ServiceConnection` for automatic database property injection.
- Do NOT use legacy `spring.factories` or manual `ContextCustomizerFactory` implementations.

### Stateless JWT Authentication
- In stateless environments, `@WithMockUser` is deprecated for Integration Tests.
- You MUST use token-based authentication by injecting the `Authorization` header.
- Use `JwtAuthenticationTestUtils` to generate valid test tokens dynamically.
- Existing legacy tests may still contain `@WithMockUser`. When modifying security-sensitive integration tests, convert them to Bearer-token-based verification first.

Example:
```java
String token = JwtAuthenticationTestUtils.createToken(login, authorities);
mockMvc.perform(get("/api/secure-endpoint")
    .header("Authorization", "Bearer " + token));
```

---

## 3. Data Integrity & Isolation

### Database Cleanup
- Explicitly handle database cleanup in `@BeforeEach` or `@AfterEach` if the test modifies shared entity state.
- Use `repository.deleteAll()` or specific service methods (e.g., `userService.deleteUser(login)`) to ensure a clean slate.

### Rate Limiting
- Ensure `rate-limit.enabled: false` is set in the test profile (e.g., `application-testdev.yml`) to prevent intermittent 429 errors during concurrent test execution.

---

## 4. Troubleshooting

### 401 Unauthorized
- Verify that the `Bearer` token is correctly generated and included in the header.
- Check if the token has the required authorities for the endpoint.

### 500 Internal Server Error
- Check `ExceptionTranslator` logs.
- Ensure custom exceptions are correctly mapped to `ProblemDetail` responses.
- Verify that no duplicate exception classes exist in the classpath.

---

## Related Documents

- [AGENTS.md](../../AGENTS.md)
- [Environment Variables Guide](environment-variables.md)
