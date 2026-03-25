# Proposal - Layered Architecture Violation Correction

## 개요 (Overview)
`DomainUserDetailsService` 가 Redis 인증 캐시를 위해 `UserAuthCacheService` 에 의존하면서 발생한 ArchUnit 위반 문제를 해결하기 위한 방안을 제안합니다.

## 가설 (Hypothesis)
`Security` 환경에서 `Service` 레이어의 공통 기능(캐싱 등)을 활용하는 것은 필수적이며, 이를 레이어간 결합으로 제한하기보다 예외 사항으로 허용하는 것이 기술적으로 타당합니다.

---

## 해결 방안 (Proposed Solutions)

### 1. `UserAuthCache` 서비스 및 DTO 패키지 이동 (ArchUnit 대응)
- 내용: `UserAuthCacheService`, `UserAuthCacheDto`를 `security` 패키지로 이동.
- 기대 효과: `Security` 레이어 내의 응집도를 높이고 레이어 위반(Security -> Service) 제거.

### 2. `UserAuthCacheDto` 클래스로 전환 (직렬화 대응)
- 내용: `record`를 일반 `class`로 변경하여 Jackson `NON_FINAL` 직렬화 호환성 확보.

### 3. `DomainUserDetailsService` 조회 로직 강화 (조회 로직 대응)
- 내용: 정규식 기반 분기 대신, 우선 이메일 시도 후 로그인 시도하는 Fallback 정책 적용.
- 기대 효과: 데이터 불일치나 정규식 판단 오류에 무관하게 안정적으로 사용자 식별.

### 4. Redisson `Duration` API 적용 (Deprecation 대응)
- 내용: `TimeUnit` 대신 `java.time.Duration` 사용하여 경고 제거.

### 5. 통합 테스트 데이터 보강
- 내용: `DomainUserDetailsServiceIT`에서 테스트 사용자에게 명시적으로 `ROLE_USER` 권한 부여.

---

## 검토 결과
사용자 피드백을 반영하여 대안 1(패키지 이동)을 최종 해결책으로 선택했습니다.
Is this the correct direction? - Confirmed by USER
