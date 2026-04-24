---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 문제 분석

## 문제 현상

Spring Boot 4 테스트 마이그레이션이 문서상으로는 완료 상태처럼 보였지만, 실제 저장소에는 세 가지 문제가 남아 있었다.

1. `verify` 경로에서 `*IT`가 실행되지 않는 구조
2. Testcontainers 선언 방식이 Spring Boot 공식 `ImportTestcontainers` 패턴과 어긋난 구조
3. 관리자 API 통합 테스트 일부가 여전히 `@WithMockUser` 기반으로 유지되는 구조

## 재현

- `pom.xml`의 Surefire 설정은 `**/*IT*`를 제외하고 있었다.
- Failsafe 설정은 존재하지 않았다.
- `IntegrationTest.java`는 Testcontainers 설정을 `@Import`로 가져오고 있었다.
- `BoardAdminResourceIT`, `TagAdminResourceIT`는 `@WithMockUser` 기반이었다.

## 원인

- 마이그레이션 방향은 맞았지만, 실행 계약과 공식 패턴 정렬이 후속 작업으로 남아 있었다.
- 문서와 실제 빌드 동작이 완전히 맞춰지지 않은 채 산출물이 먼저 정리되었다.

## 영향

- `./mvnw test`만으로도 통합 테스트까지 검증한 것처럼 오해할 수 있었다.
- 보안 핵심 관리자 API에서 JWT 필터 체인 회귀를 놓칠 수 있었다.
- 다른 에이전트가 잘못된 기준으로 완료 보고를 남길 가능성이 있었다.

