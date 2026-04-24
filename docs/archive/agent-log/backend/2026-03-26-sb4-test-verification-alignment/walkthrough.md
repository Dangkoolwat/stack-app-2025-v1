---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 작업 흐름

## 1. 빌드 계약 정리

먼저 `pom.xml`에서 Surefire가 `*IT`를 제외하고 있다는 점을 기준으로, `verify`에서 통합 테스트를 수행할 수 있도록 Failsafe 플러그인을 추가했다.

이를 통해 `test`와 `verify`의 역할이 분리되었다.

## 2. Testcontainers 구조 정리

기존에는 `TestcontainersConfiguration` 내부에 static 컨테이너와 동적 프로퍼티가 함께 있었다.

이를 `IntegrationTestContainers` 선언 인터페이스와 `@ImportTestcontainers` 기반 구성으로 분리해 Spring Boot 공식 패턴에 맞췄다.

그리고 `IntegrationTest.java`는 필요한 테스트 설정 클래스를 `@SpringBootTest(classes=...)`로 명시하도록 정리했다.

## 3. 보안 테스트 전환

대표 관리자 통합 테스트인 `BoardAdminResourceIT`, `TagAdminResourceIT`를 `@WithMockUser` 대신 `JwtAuthenticationTestUtils` 기반 Bearer 토큰 방식으로 바꿨다.

이렇게 해서 관리자 권한/일반 사용자 권한 분기가 실제 JWT 인증 헤더 경로를 통과하도록 만들었다.

## 4. 문서와 잔존물 정리

- `logback.xml`의 레거시 `RedisTestContainer` 로거를 제거했다.
- `docs/operations/testing-guideline.md`에 `test`와 `verify` 역할을 구분해 추가했다.
- `AGENTS.md`에도 unit/non-IT와 full validation 명령 예시를 분리해 기록했다.

## 5. 검증

테스트 소스 컴파일을 먼저 통과시킨 뒤, 대표 IT 두 개를 Failsafe 경로로 실제 실행했다.

결과는 `target/failsafe-reports`에서 다음처럼 확인했다.

- `BoardAdminResourceIT`: 5 tests, 0 failures, 0 errors
- `TagAdminResourceIT`: 4 tests, 0 failures, 0 errors

