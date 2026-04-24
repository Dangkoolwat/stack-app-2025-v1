# 2026-03-17 Cache & Security Configuration Optimization Summary

## Date
2026-03-17

## Task Title
2차 리뷰 대응 및 캐시/보안 설정 최적화

## Work Performed

### 1. 보안 강화 (NC-1: Critical)
- 운영 환경(prod) 보안성 확보: `application-prod.yml`에서 JWT Secret의 fallback(기본값)을 완전히 제거하여, `JWT_SECRET` 환경변수가 주입되지 않으면 애플리케이션이 구동되지 않도록 강제했습니다.
- 개발 환경(dev) 격리: `application-dev.yml`에 prod와 완전히 분리된 개발용 시크릿을 지정하여 로컬 개발 편의성과 보안을 동시에 확보했습니다.

### 2. Redisson 직렬화 방식 개선 (NM-5: Medium)
- 보안 및 성능 최적화: 기존 Java 기본 직렬화(`SerializationCodec`)를 보안상 안전하고 속도가 빠른 `JsonJacksonCodec`으로 전환했습니다.
- 영향: 엔티티 직렬화 공격(Serialization attack)을 원천 방지하며 대용량 데이터 처리 효율을 개선했습니다.

### 3. Redis 모니터링 가용성 및 테스트 해결 (NH-2)
- Health Indicator 로직 고도화: Redis `info` 명령이 실패하더라도 `ping`이 성공하면 UP 상태를 반환하도록 수정한 결과, `RedisMonitoringConfigurationTest`의 모든 실패 테스트가 성공으로 전환되었습니다.
- 클러스터/센티널 대응: Single, Cluster, Sentinel 모드 각각에 맞게 노드를 감지하는 `findActiveNode` 메서드를 안정화했습니다.

### 4. IDE 심볼 오류 및 Jakarta EE 대응
- 의존성 충돌 해결: Redisson 4.3.0이 내부적으로 `javax.cache` 인터페이스를 사용하는 점을 고려하여, `pom.xml`에 `javax.cache:cache-api:1.1.1` 의존성을 명시적으로 추가했습니다.
- 패키지 정합성 유지: `CacheConfiguration.java`를 Redisson의 타입 체계에 맞는 `javax.cache` 기반으로 복구하여 IDE 심볼 미인식 문제를 완벽히 해결했습니다.

## Files Modified
- `pom.xml` (의존성 추가 및 버전 관리)
- `src/main/resources/config/application-dev.yml` (JWT 시크릿 관리)
- `src/main/resources/config/application-prod.yml` (JWT 시크릿 강제화)
- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java` (코덱 변경 및 패키지 마이그레이션)
- `src/main/java/com/daangcool/stack/config/RedisMonitoringConfiguration.java` (헬스 체크 로직 개선)

## Verification
- `./mvnw clean compile`: SUCCESS (IDE 심볼 및 컴파일 오류 해결 확인)
- `./mvnw test -Dtest=RedisMonitoringConfigurationTest`: SUCCESS (모든 테스트 통과)

## Remaining Risks
- 캐시 포맷 변경: `JsonJacksonCodec` 도입으로 인해 기존 Redis에 저장된 데이터와 포맷이 호환되지 않습니다. 운영 배포 시 반드시 `FLUSHDB` 등 캐시 무효화 작업이 필요합니다.
