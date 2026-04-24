---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 문제 분석

## 1. 개요
Spring Boot 4 마이그레이션 중 테스트 인프라에서 다음과 같은 문제 발생:
- 레거시 Testcontainers 설정(Spring Factory 기반)이 Spring Boot 4 방식과 충돌.
- 통합 테스트에서 JWT 인증 실패(401) 및 데이터 무결성 문제(400/500/429) 빈번.
- 불필요한 레거시 팩토리와 설정 파일들이 산재해 있어 유지보수 어려움.

## 2. 주요 현상
- `SettingsResourceIT`: JWT 토큰 누락으로 인한 401 Unauthorized 발생.
- `AccountResourceIT`: 
    - `@WithMockUser` 미동작 (Stateless JWT 환경).
    - 중복 가입 테스트에서 500 에러 발생 (잘못된 예외 처리 및 중복 구현된 Exception 클래스).
    - Rate Limit 필터로 인한 429 에러.
- 테스트 간 데이터 공유: Oracle 컨테이너 내부 데이터가 잔류하여 `assertThat(count).isEqualTo(0L)` 실패.

## 3. 원인 분석
- **인증**: Stateless JWT 환경에서 Spring Security의 MockUser가 컨텍스트를 올바르게 채우지 못함. 직접적인 `Authorization: Bearer <token>` 헤더 주입 필요.
- **인프라**: `spring.factories`를 통한 커스텀 `ContextCustomizerFactory` 방식이 최신 Spring Boot Testcontainers 자동 설정과 충돌.
- **예외 처리**: `UsernameAlreadyUsedException`과 `InvalidPasswordException`이 `com.daangcool.stack.service`와 `com.daangcool.stack.common.exception`에 중복 정의되어 있었고, 리소스/서비스에서 로컬 버전을 사용하면서 `ExceptionTranslator`가 이를 정상적으로 400으로 변환하지 못하고 500으로 배출함.
- **환경 설정**: `RateLimitingFilter`가 테스트 환경에서도 활성화되어 동시 테스트 실행 시 차단됨.
