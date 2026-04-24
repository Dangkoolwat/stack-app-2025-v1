# Spring Boot 4 Testing Standards (MANDATORY)

To ensure high performance and security in the modern Spring Boot 4 environment, all agents MUST follow these patterns:

## Infrastructure

- Use `@IntegrationTest` (composite annotation) for all integration tests.
- Leverage `spring-boot-testcontainers` with `@ServiceConnection` for automatic property injection.
- Do NOT use legacy `spring.factories` or custom `ContextCustomizerFactory` for Testcontainers.

## Authentication

- Stateless JWT environments MUST NOT use `@WithMockUser`.
- Use token-based authentication with `Bearer` header for all protected API tests.
- Generate tokens dynamically using `JwtAuthenticationTestUtils`.

## Data Integrity

- Explicitly handle database cleanup (e.g., `userRepository.deleteAll()`) in `@BeforeEach` or `@AfterEach`.
- Ensure tests are truly isolated to prevent flaky results in shared container environments.

## Performance

- Disable Rate Limiting in test profiles (e.g., `rate-limit.enabled: false`) to prevent intermittent 429 errors.
- Reference [Testing Guideline](docs/operations/testing-guideline.md) for detailed implementation patterns.
