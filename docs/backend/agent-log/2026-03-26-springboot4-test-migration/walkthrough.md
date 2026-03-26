---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 워크스루

## 1. 구현 흐름 요약
1. **인프라 기초 공사**: `pom.xml`과 `IntegrationTest.java`를 수정하여 Spring Boot 4의 `@ServiceConnection` 방식을 도입했습니다.
2. **보안 인증 전환**: `AccountResourceIT`와 `SettingsResourceIT`에서 `@WithMockUser`를 제거하고, `JwtAuthenticationTestUtils`를 통해 생성된 토큰을 Bearer 헤더에 직접 주입하도록 하였습니다.
3. **데이터 및 예외 정합성**: 
   - `userRepository.deleteAll()`을 사용하여 확실한 데이터 초기화를 수행했습니다.
   - `UsernameAlreadyUsedException` (local) 삭제 및 `LoginAlreadyUsedException` (common) 통합으로 500 에러를 400 에러로 정상화했습니다.
   - `InvalidPasswordException`이 `BadRequestAlertException`을 상속하게 하여 예외 응답 형식을 통일했습니다.
4. **레거시 제거**: `@ContextCustomizerFactory` 기반의 모든 레거시 클래스와 `spring.factories` 파일을 삭제하여 설정을 단순화했습니다.

## 2. 핵심 구현 포인트
- `TestcontainersConfiguration.java`: Oracle과 Redis 이미지를 중앙에서 관리하며, Oracle은 `@ServiceConnection`으로, Redis는 별도의 `@DynamicPropertySource`로 연동되도록 구성했습니다.
- `RateLimitingFilter.java`: 테스트 시 rate limit을 끌 수 있게 프로퍼티 연동을 추가하여 안정성을 높였습니다.

## 3. 검증 결과
- **전체 테스트 수**: 119건
- **성공**: 119건
- **실패**: 0건
- **수행 시간**: 약 35초 (단일 모듈 기준)
- **확인 사항**: 모든 테스트 클래스에서 `Spring Boot v4.0.4` 로딩 및 Testcontainers 환경 변수 주입 확인.
