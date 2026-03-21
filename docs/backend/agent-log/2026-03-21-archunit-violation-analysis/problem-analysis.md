# Problem Analysis - TechnicalStructureTest ArchUnit Violation

## 문제 현상 (Phenomenon)
`./mvnw clean test` 실행 시 `TechnicalStructureTest.respectsTechnicalArchitectureLayers` 테스트에서 `AssertionError`가 발생합니다.

- **발생 지점**: `com.daangcool.stack.TechnicalStructureTest`
- **위반 횟수**: 15회
- **주요 위반 내용**: `Security` 레이어(`..security..`)가 `Service` 레이어(`..service..`)를 참조함.

## 재현 (Reproduction)
1. 터미널에서 `./mvnw clean test -Dtest=TechnicalStructureTest` 명령 실행.
2. ArchUnit Rule 검증 단계에서 실패 확인.

## 상세 원인 분석 (Detailed Root Cause)

### 1. 아키텍처 위반 (ArchUnit Failure)
인증 조회를 위한 Redis 2차 캐시 도입 과정에서 `Security` 레이어(`DomainUserDetailsService`)가 `Service` 레이어(`UserAuthCacheService`)를 직접 의존하게 되었습니다. 하지만 프로젝트의 기본 아키텍처 규칙상 `Service`는 `Web`과 `Config`에서만 접근 가능하도록 제한되어 있어 위반이 발생했습니다.

### 2. 인증 캐시 조회 실패 (Serialization Issue)
- **현상**: 캐시 `put`은 성공하나 `get` 시 항상 `Optional.empty()`가 반환됨.
- **원인**: `UserAuthCacheDto`가 `record` (final class)로 구현되어 있어 Jackson 3의 `NON_FINAL` 타이핑 모드와 충돌합니다. JSON 저장 시 `@class` 정보가 누락되어 역직렬화에 실패합니다.

### 3. 사용자 식별자 조회 로직 실패 (Fragile Lookup Logic)
- **현상**: `test-user-one@localhost` 형태의 식별자로 로그인 시 사용자가 있음에도 `UsernameNotFoundException` 발생.
- **원인**: `if-else` 분기 기반의 조회가 엄격하여, 정규식 판단 오류나 DB 데이터 불일치(Login vs Email) 상황에서 유연하게 대응하지 못합니다.

### 4. 테스트 데이터 권한 누락 (Test Data Issue)
- **현상**: 통합 테스트에서 사용자의 권한 정보가 비어있음.
- **원인**: `getUserOne()` 등의 팩토리 메서드에서 `Authorities`를 설정하지 않은 채 저장하고 있습니다.

### 5. Deprecated API 경고 (Maintenance Issue)
- **현상**: `UserAuthCacheService` 컴파일 시 경고 발생.
- **원인**: Redisson 최신 버전에서 `long/TimeUnit` 기반의 `set` 메서드가 Deprecated되고 `Duration` 기반 메서드로 교체되었습니다.

## 영향 (Impact)
- 전체 빌드 중단 (ArchUnit).
- 인증 속도 저하 (2차 캐시 무력화).
- 특정 형식의 사용자 로그인 불가.
- 유지보수 시 경고 발생 및 잠재적 런타임 오류.
