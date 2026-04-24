---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 구현 계획

## 1. 인프라 현대화
- [x] pom.xml: `spring-boot-testcontainers` 추가, `testcontainers-jdbc` 제거.
- [x] IntegrationTest.java: `@ServiceConnection` 기반 자동 설정 적용.
- [x] TestcontainersConfiguration.java: Oracle, Redis 컨테이너 통합 시각화.

## 2. 보안 인증 마이그레이션
- [x] AccountResourceIT.java: `@WithMockUser` → `createToken` 기반 Bearer 인증 전환.
- [x] SettingsResourceIT.java: ObjectMapper 및 토큰 인증 적용.

## 3. 예외 및 필터링 수정
- [x] UserService.java: `UsernameAlreadyUsedException` (local) 삭제 → `LoginAlreadyUsedException` (common) 통합.
- [x] InvalidPasswordException.java: `BadRequestAlertException` 상속으로 변경 및 불필요한 JHipster 빌더 제거.
- [x] RateLimitingFilter.java: 테스트 환경에서 비활성화 가능하도록 프로퍼티 토글 추가.

## 4. 레거시 제거 및 최종 검증
- [x] 8개의 커스텀 팩토리 및 `spring.factories` 삭제.
- [x] 전체 119개 테스트 실행 및 검증.
