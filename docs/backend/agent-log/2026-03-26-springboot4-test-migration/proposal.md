---
agent: Antigravity
created_at: 2026-03-26 (Thu)
language: ko
---

# 제안 및 해결 방안

## 1. 해결 전략
- **인증 현대화**: `@WithMockUser` 대신 `JwtAuthenticationTestUtils`를 활용한 토큰 기반 인증으로 전면 전환.
- **인프라 통합**: `spring-boot-testcontainers` 의존성을 도입하고 `IntegrationTest` 어노테이션에서 `@ServiceConnection`을 활용하여 수동 팩토리 제거.
- **예외 통합**: 서비스 계층(UsernameAlreadyUsedException)과 공용 예외(LoginAlreadyUsedException)를 하나로 통합하여 `ExceptionTranslator` 매핑 일관성 확보.
- **데이터 정리**: 각 테스트 시작 전 `userRepository.deleteAll()` 및 `@Transactional`을 명시하여 데이터 오염 방지.
- **환경 조정**: `application-testdev.yml`에서 `rate-limit.enabled: false`를 설정하여 차단 방지.

## 2. 대안 검토
- **대안 1**: `@WithMockUser`를 커스터마이징하여 JWT Security Context를 강제 주입.
  - 리스크: JHipster/Spring Security 내부 구현에 너무 의존하게 됨.
- **대안 2**: `@DataJpaTest`와 `@SpringBootTest`를 분리하여 실행.
  - 리스크: 전체 컨텍스트 로딩 시간이 늘어나고 통합 테스트의 의미가 퇴색됨.

## 3. 최종 선택 이유
- **토큰 기반 인증**은 실제 클라이언트와 서버 간의 API 계약을 가장 정확하게 시뮬레이션하므로 보안 검증에 최적임.
- **Spring Boot 4 표준 라이브러리** 활용은 향후 유지보수성 및 버전 업데이트 대응에 가장 유리함.
