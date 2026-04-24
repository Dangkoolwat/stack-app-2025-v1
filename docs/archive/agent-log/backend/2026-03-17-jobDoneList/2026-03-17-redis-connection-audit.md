# 2026-03-17 Redis 연결(중복/불필요) 점검 및 정리

## 기본 정보

- Date: 2026-03-17
- Agent: Codex (GPT-5.2)
- Task Title: Redis/Redisson 중복 접속 및 라이프사이클 점검
- Goal: Redis 관련 설정/빈 구성에서 불필요한 이중 접속(중복 RedissonClient 생성) 여부를 확인하고, 필요 시 안전한 개선을 적용한다.

## Context

- 프로젝트는 Redis 연동을 Spring Data Redis(Lettuce/Jedis) 가 아니라 Redisson 중심으로 사용.
- 과거 이슈: `CacheConfiguration`에서 `redissonClient`와 `jcacheConfiguration`이 각각 `Redisson.create()`를 호출해 이중 연결이 발생했으나, 2026-03-14 작업으로 `RedissonConfiguration.fromInstance(redissonClient, ...)`로 통합됨.
  - 참고: `docs/agent-log/2026-03-14-sb4-migration-review.md`

## Work Performed

- `Redisson.create()` 호출 지점 및 Redis 관련 설정을 전수 검색.
- dev/prod 설정에서 `application.rate-limit.redis-server` 사용 여부를 확인하여 전용 RedissonClient 추가 생성 조건을 확인.
- RedissonClient 인터페이스를 확인해(4.3.0) `close()`가 없고 `shutdown()`만 존재함을 확인 → Spring bean 종료 시 자동 종료가 보장되지 않음을 확인.
- 기본 캐시용 `RedissonClient` 빈에 `destroyMethod="shutdown"`을 추가해 정상 종료 시 연결 정리를 보장.

## Findings

1. (정상) CacheConfiguration 이중 생성은 현재 없음
   - `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`는 `redissonClient` 단일 빈을 만들고, JCache 설정은 `fromInstance(redissonClient, ...)`로 재사용.

2. (조건부) RateLimiting 전용 Redis 설정이 있으면 별도 Redisson 연결 추가 생성
   - `src/main/java/com/daangcool/stack/config/RateLimitingConfiguration.java`는 `application.rate-limit.redis-server`가 비어있지 않으면 `Redisson.create()`로 전용 클라이언트를 생성.
   - `src/main/resources/config/application-dev.yml`은 `application.rate-limit.redis-server: redis://localhost:6379`로 설정되어 있고, 동시에 `jhipster.cache.redis.server`도 기본값이 `redis://localhost:6379`라서 동일 Redis에 대해 RedissonClient가 2개 생성될 수 있음(캐시용 + RateLimit용).
   - 이중 접속이 “불필요”한지 여부는 운영 의도(풀 분리/격리) 여부에 따라 달라서, 코드 변경 없이도 dev 설정에서 `application.rate-limit.redis-server`를 비우면 기본 캐시용 RedissonClient를 재사용 가능.

3. (개선) RedissonClient 종료 처리
   - RedissonClient(4.3.0)는 `shutdown()`만 제공 → `@Bean`에 `destroyMethod`를 명시하지 않으면 Spring 종료 시 자동 연결 종료가 보장되지 않음.

## Files Modified

- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- `docs/agent-log/2026-03-17-redis-connection-audit.md`

## Architecture Impact

- No architectural changes.

## Security Impact

- No security impact.

## Verification

- `./mvnw test`

## Risks

- `application.rate-limit.redis-server`가 실제 운영/스테이징에서 값이 세팅되고(특히 캐시용 Redis와 동일한 값), 풀 분리가 의도된 것이 아니라면 불필요한 Redis 연결/리소스 증가가 발생할 수 있음.
- `RateLimitingConfiguration`에서 생성되는 전용 RedissonClient는 Spring bean이 아니라서(현 구조) `shutdown()` 호출이 누락될 수 있음(프로세스 종료 시 OS가 정리하긴 하나, graceful shutdown 관점에서는 개선 여지).

## Next Suggested Tasks

1. Rate limit 전용 Redis가 캐시용 Redis와 동일한 경우:
   - dev/prod 설정에서 `application.rate-limit.redis-server`를 비워 기본 RedissonClient 재사용 검토.
2. 전용 Redis(또는 전용 풀) 유지가 필요하면:
   - 전용 RedissonClient를 Spring `@Bean(destroyMethod="shutdown")`로 승격하고 `@Qualifier`로 명확히 주입되도록 리팩터 검토(다중 `RedissonClient` 빈 주입 모호성 방지 포함).

## Notes for Future Agents

- Redis 모니터링(`RedisMonitoringConfiguration`)은 `RedisNodes.SINGLE` 기반이므로, 실제 운영이 클러스터 모드인 경우 동작/지표 수집 방식 점검이 필요할 수 있음(본 작업 범위에서는 변경하지 않음).

