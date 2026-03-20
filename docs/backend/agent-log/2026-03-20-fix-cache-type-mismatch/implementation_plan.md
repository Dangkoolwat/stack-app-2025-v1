# Implementation Plan - Fix Cache Type Mismatch in GlobalSettingsService

The `GlobalSettingsService.getSettings()` method throws an `IllegalStateException` because the cached value is deserialized as a `Map` instead of `SettingsDTO`. This is caused by the lack of polymorphic type information in the Redis JSON storage after the Jackson 3 migration.

An exhaustive investigation (전수조사) revealed that this issue is **systemic** and potentially affects all application-level caches using the `redissonJsonClient`, including `BoardService`, `TagService`, `UploadService`, and `CommonCodeService`.

## Proposed Changes

### [Backend] [Component: Cache]

#### [MODIFY] [CacheConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/CacheConfiguration.java)
- Create a dedicated `ObjectMapper` for Redisson (e.g., `redisObjectMapper`).
- Enable Jackson 3 `DefaultTyping` on this `ObjectMapper` to include type information in the stored JSON.
- Use `BasicPolymorphicTypeValidator` for security.
- Update `redissonJsonClient` to use this new `ObjectMapper`.

#### [NEW] [RedisSerializationIT.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/test/java/com/daangcool/stack/config/RedisSerializationIT.java)
- Create an integration test that uses `redissonJsonClient` to store and retrieve a `SettingsDTO`.
- This test will reproduce the `IllegalStateException` before the fix and verify the fix after.

## Verification Plan

### Automated Tests
- Run the new integration test:
  ```bash
  ./mvnw test -Dtest=RedisSerializationIT
  ```
- Run existing cache and service tests:
  ```bash
  ./mvnw test -Dtest=CacheConfigurationIT,GlobalSettingsServiceIT
  ```

### Manual Verification
- Clear the Redis cache completely to ensure no stale data without type info remains:
  ```bash
  redis-cli flushall
  ```
- Restart the application and verify that `/api/settings` returns the correct data without errors.
