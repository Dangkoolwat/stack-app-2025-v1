# 2026-03-16 W-1 Rate Limiting Implementation

- Date: 2026-03-16
- Agent: Antigravity
- Task Title: W-1 Rate Limiting Implementation
- Goal: Implement rate limiting for specific API endpoints to prevent brute-force and email enumeration attacks.

## Context
High-risk endpoints (`/api/authenticate`, `/api/register`, `/api/account/reset-password/init`) were found to be without rate limiting, exposing them to automated attacks.

## Work Performed
1.  Dependency: Added `bucket4j-core:8.10.1` to `pom.xml`.
2.  Exception Handling:
    -   Created `TooManyRequestsException` (429 status).
    -   Added `TOO_MANY_REQUESTS_TYPE` to `ErrorConstants`.
    -   Added `@ExceptionHandler` in `ExceptionTranslator` as a service-layer safety net.
3.  Security Filter:
    -   Implemented `RateLimitingFilter` extending `OncePerRequestFilter`.
    -   Uses Token Bucket algorithm (Bucket4j).
    -   Configured IP-based policies for the three target endpoints.
    -   Writes RFC 7807 compliant `ProblemDetail` directly to response.
4.  Security Configuration:
    -   Registered `RateLimitingFilter` before `BasicAuthenticationFilter`.
    -   Injected `ObjectMapper` for JSON response writing.
5.  Verification:
    -   Created `RateLimitingFilterTest` with 7 unit test cases (Pass/429/IP-independent/X-Forwarded-For/etc.).
    -   All unit tests passed.

## Files Modified
-   `pom.xml`
-   `src/main/java/com/daangcool/stack/common/constant/ErrorConstants.java`
-   `src/main/java/com/daangcool/stack/common/exception/TooManyRequestsException.java` [NEW]
-   `src/main/java/com/daangcool/stack/web/rest/errors/ExceptionTranslator.java`
-   `src/main/java/com/daangcool/stack/web/filter/RateLimitingFilter.java` [NEW]
-   `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
-   `src/test/java/com/daangcool/stack/web/filter/RateLimitingFilterTest.java` [NEW]

## Architecture & Security Impact
-   Security: Significantly improves resilience against brute-force and enumeration. Standardized 429 error responses.
-   Architecture: Added a new filter at the entry point of the security chain. Uses in-memory storage for buckets, suitable for single-instance scaling.

## Verification
-   `RateLimitingFilterTest`: 7/7 tests passed.
-   `./mvnw test -Dtest=RateLimitingFilterTest` - [OK]

## Risks & Next Steps
-   Risks: In-memory storage will reset on restart. For distributed environments, `bucket4j-redis` should be considered.
-   Next Tasks: Monitor 429 logs in production to fine-tune thresholds. Consider adding rate limiting to OTP endpoints if needed.

## Notes for Future Agents
-   The filter handles `429` directly to avoid proceeding to authentication logic.
-   Bucket4j version `8.10.1` is used (latest stable on Central).
