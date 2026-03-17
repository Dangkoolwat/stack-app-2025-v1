# 2026-03-17 Rate Limiting Refactoring & Security Hardening (2nd Review)

## Date
2026-03-17

## Agent
Antigravity (Codex)

## Task Title
Rate Limiting 리팩토링 및 보안 강화 (2차 리뷰 조치)

## Goal
2차 리뷰 리포트에서 지적된 기술적 부채 및 보안 이슈를 해결합니다.
1. Bucket4j 제거 및 Redisson Native 기반 Rate Limiting 전환 (NH-1)
2. OTP 로그 기록의 트랜잭션 독립성 보장 (NC-2)
3. OTP 코드 마스킹 처리 (NH-3)
4. Prometheus 엔드포인트 접근 제한 (NH-4)
5. Redis 모니터링의 클러스터/센티널 모드 지원 (NH-2)

## Context
- 기존 Bucket4j 구현체는 Redisson 내부 API를 리플렉션으로 접근하여 버전 업그레이드 시 취약점이 있었습니다.
- OTP 로그 기능이 동일 클래스 내 self-invocation 문제로 인해 주 트랜잭션 실패 시 함께 롤백되는 문제가 있었습니다.
- 모니터링 엔드포인트 중 민감한 지표가 외부에 노출되어 있었습니다.

## Work Performed
1.  **Rate Limiting 리팩토링 (NH-1)**:
    - `RateLimitingRegistry`를 Redisson `RRateLimiter` 기반으로 재구현.
    - `pom.xml`에서 `bucket4j-core`, `bucket4j-redis` 의존성 제거.
    - `RateLimitingFilter` 및 `RateLimitingFilterTest` 최신 API에 맞춰 업데이트.
2.  **Audit Log 신뢰성 개선 (NC-2)**:
    - `EmailOtpLogService`를 분리하여 `@Transactional(REQUIRES_NEW)`가 정상 작동하도록 수정.
    - 주 로직(메일 발송 등) 실패와 관계없이 감사 로그가 독립적으로 기록됨을 보장.
3.  **보안 강화 (NH-3, NH-4)**:
    - `EmailOtpLogService`에서 기록 시 OTP 코드를 마스킹 (`12****`) 처리.
    - `SecurityConfiguration`에서 `/management/prometheus`를 ADMIN 권한으로 제한.
4.  **모니터링 고도화 (NH-2)**:
    - `RedisMonitoringConfiguration`에서 Single, Cluster, Sentinel 모드를 모두 지원하도록 노드 감지 로직 개선.
    - Redisson 4.3.0 `getRedisNodes` API를 사용하여 하위 호환성 및 안정성 확보.
5.  **캐시 최적화 (NL-5)**:
    - `EmailOtpLog` 엔티티에서 불필요한 Hibernate 2차 캐시 설정을 제거하여 성능 및 정합성 관리 효율을 높임.

## Files Modified
- `pom.xml`
- `src/main/java/com/daangcool/stack/security/RateLimitingRegistry.java`
- `src/main/java/com/daangcool/stack/config/RateLimitingConfiguration.java`
- `src/main/java/com/daangcool/stack/web/filter/RateLimitingFilter.java`
- `src/test/java/com/daangcool/stack/web/filter/RateLimitingFilterTest.java`
- `src/main/java/com/daangcool/stack/service/otp/EmailOtpService.java`
- `src/main/java/com/daangcool/stack/service/otp/EmailOtpLogService.java` (New)
- `src/main/java/com/daangcool/stack/config/SecurityConfiguration.java`
- `src/main/java/com/daangcool/stack/config/RedisMonitoringConfiguration.java`
- `src/main/java/com/daangcool/stack/domain/EmailOtpLog.java`
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`

## Architecture Impact
- **Layering**: 로깅 기능을 전담하는 `EmailOtpLogService`를 추가하여 관심사를 분리하고 트랜잭션 경계를 명확히 함.
- **Dependency**: 외부 라이브러리(Bucket4j) 의존성을 제거하고 인프라 라이브러리(Redisson)의 공용 API를 최대한 활용.

## Security Impact
- **Data Privacy**: 로그 내 OTP 노출 차단.
- **Access Control**: 시스템 지표 노출 엔드포인트 보호 강화.

## Verification
- `RateLimitingFilterTest` 전체 통과 확인.
- Redisson 4.3.0 API 정합성 확인 (스크래치 스크립트 기반).
- `EmailOtpLogService` 추출을 통한 트랜잭션 전파 구조 검증.

## Risks
- Redis 클러스터/센티널 환경에서 `info memory` 명령 권한이 없는 경우 Health 지표 추출이 제한될 수 있음 (UP 상태는 유지됨).

## Next Suggested Tasks
- 현재 6자리 숫자인 OTP의 복잡성 강화 고려.
- Redis 사용량 임계치 초과 시 SMS/Email 알림 발송 기능 검토.

## Notes for Future Agents
- Redisson 4.x에서는 `getNodesGroup()` 대신 `getRedisNodes()`를 사용하는 것이 표준입니다. 
- 복잡한 분산 락이나 상태 체크 시 `RedisSingle`, `RedisCluster`, `RedisMasterSlave` 등 구체 타입의 API 차이를 유의하십시오.
