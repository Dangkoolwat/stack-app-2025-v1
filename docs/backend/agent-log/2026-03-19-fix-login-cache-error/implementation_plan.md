# Implementation Plan: Fix Login Cache Error

The user is experiencing a `com.fasterxml.jackson.databind.exc.InvalidTypeIdException: Could not resolve subtype ... missing type id property '@class'` error when logging in. This is related to Redisson's `JsonJacksonCodec` configuration and how it interacts with Jackson's polymorphic deserialization.

## User Review Required
> [!IMPORTANT]
> This error often occurs when the `JsonJacksonCodec` is configured to expect type information (`@class`) that is missing from the stored JSON. It can also happen if there is stale data in Redis from a different codec or configuration.

## Proposed Changes

### [Component Name] Cache Configuration
I will update `CacheConfiguration.java` to use the application's `ObjectMapper` for `JsonJacksonCodec`. This ensures that all type information and serialization settings are consistent across the application and the cache.

#### [MODIFY] [CacheConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/CacheConfiguration.java)
- Update `redissonClient` bean to accept `ObjectMapper` as a parameter.
- Pass the `ObjectMapper` to `JsonJacksonCodec`.

## Verification Plan

### Automated Tests
- Run `AuthenticateControllerIT` to verify the login flow with a real (Testcontainers) Redis.

### Manual Verification
- Ask the user to run `FLUSHALL` in their local Redis to rule out stale data issues.
- Verify that the login error is resolved after the fix.
