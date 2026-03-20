# 2026-03-17 Redis 설정 SSOT 도입 + 단일 RedissonClient 재사용

## 기본 정보

- **Date:** 2026-03-17
- **Agent:** Codex (GPT-5.2)
- **Task Title:** Redis 설정 단일 출처(SSOT) 및 불필요한 추가 연결 제거
- **Goal:** Redis 서버는 필수이며(single/cluster만 차이), 캐시/락/레이트리밋 등 Redis 사용 기능이 늘어도 불필요한 Redis 연결이 증가하지 않도록 구성 정리.

## Context

- 캐시/락/OTP는 `RedissonClient`를 통해 Redis를 사용.
- 기존 `RateLimitingConfiguration`은 `application.rate-limit.redis-server`가 설정되면 `Redisson.create()`로 전용 클라이언트를 추가 생성할 수 있었고, dev 설정에서 실제로 값이 지정되어 있어 같은 Redis에 대해 연결 풀이 중복될 수 있었음.
- 요구사항: Redis는 필수. 싱글/클러스터만 차이. 기능별 별도 Redis/별도 연결 풀은 기본적으로 원치 않음.

## Work Performed

- `application.redis.*` 프로퍼티 그룹을 추가하여 Redis 설정의 단일 출처(SSOT)를 마련.
- `application-dev.yml`, `application-prod.yml`에서 `jhipster.cache.redis.*`가 `application.redis.*`를 참조하도록 변경.
- `RateLimitingConfiguration`에서 전용 RedissonClient 생성 로직을 제거하고, 기본 캐시용 `RedissonClient`를 항상 재사용하도록 변경.
- dev에서 `application.rate-limit.redis-server` 고정값을 제거하여 실수로 전용 연결이 생기는 경로를 차단.
- `application.yml`의 `application` 섹션에서 `rate-limit` 블록이 잘못 중첩될 수 있는 구성을 바로잡고(`application.rate-limit.*` 형태로 복원) 기본 설정 문서의 바인딩 안전성을 확보.
- 기본 설정(`application.yml`)에서 더 이상 사용되지 않는 `application.rate-limit.redis-server/cluster` 항목을 제거하여 오해의 소지를 줄임.
- `application.yml`의 `application.redis.server`에 `APPLICATION_REDIS_SERVER`를 기본으로 하고, 기존 `.env`에서 사용하던 `JHIPSTER_CACHE_REDIS_HOST`를 fallback으로 두어 환경변수 마이그레이션 부담을 낮춤.

## Files Modified

- `src/main/resources/config/application.yml`
- `src/main/resources/config/application-dev.yml`
- `src/main/resources/config/application-prod.yml`
- `src/main/java/com/daangcool/stack/config/ApplicationProperties.java`
- `src/main/java/com/daangcool/stack/config/RateLimitingConfiguration.java`

## Architecture Impact

- No architectural changes. (구성/프로퍼티 정리 및 연결 재사용 정책 강화)

## Security Impact

- No security impact.

## Verification

- `./mvnw test`

## Risks

- `application.redis.server`는 클러스터 사용 시 콤마 구분 문자열(또는 환경변수)로 여러 URL을 지정하는 형태를 전제로 함.
- `application.rate-limit.redis-server`는 설정 파일에 남아있지만, 현재 `RateLimitingConfiguration`에서는 더 이상 사용하지 않음(호환성 유지용).

## Next Suggested Tasks

- 운영/스테이징에서 Redis 클러스터 사용 시 `APPLICATION_REDIS_SERVER`(comma-separated) 및 `APPLICATION_REDIS_CLUSTER=true`로 통일하고, 기존 `JHIPSTER_CACHE_REDIS_HOST` 사용 여부를 정리.
- 필요 시 `application.rate-limit.*`에서 `redis-server/cluster` 필드를 제거(단, `@ConfigurationProperties(ignoreUnknownFields=false)` 정책과의 호환성 검토 후 진행).

## Notes for Future Agents

- `jhipster.cache.redis.server`는 JHipsterProperties에서 `String[]`로 바인딩되며, comma-separated string 값도 정상적으로 배열로 변환됨.
