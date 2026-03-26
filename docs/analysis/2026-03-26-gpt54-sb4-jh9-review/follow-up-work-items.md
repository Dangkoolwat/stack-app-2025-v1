---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 후속 작업 세부 항목

## 목표

Spring Boot 4 + JHipster 9 마이그레이션 이후 테스트 인프라와 문서 상태를 실제 코드 기준으로 정렬한다.

## 작업 스트림 A. 테스트 실행 계약 정리

### 작업 A-1. IT 실행 경로 확정

- `maven-surefire-plugin`과 `maven-failsafe-plugin` 역할을 재정의한다.
- `*IT` 클래스가 언제 어떤 명령으로 실행되는지 팀 표준을 문서화한다.
- `docs/operations/testing-guideline.md`와 `AGENTS.md`의 테스트 명령 예시를 실제 빌드 경로와 맞춘다.

완료 기준

- `./mvnw test`와 `./mvnw verify`의 역할 차이가 명확하다.
- IT 실행 여부가 로그와 리포트로 증명된다.

### 작업 A-2. 검증 증적 저장 방식 명문화

- "119개 통과" 같은 수치는 어떤 명령 결과인지 명시한다.
- 가능하면 `surefire-reports` 또는 `failsafe-reports` 기준을 agent-log에 남긴다.
- 이후 agent-log의 self-check는 실제 수행한 명령만 체크하도록 정리한다.

완료 기준

- 최종 보고서에 테스트 수치의 출처가 포함된다.
- self-check의 테스트 통과 항목이 실행 근거와 연결된다.

## 작업 스트림 B. Testcontainers 패턴 정렬

### 작업 B-1. `TestcontainersConfiguration` 구조 재검토

선택지

1. `@Bean` 기반으로 Oracle/Redis 컨테이너를 선언하고 Oracle에 `@ServiceConnection` 적용
2. static declaration class 패턴을 유지하되 `@ImportTestcontainers` 사용

권장

선언 방식 하나로 통일하고, Redis는 필요 시 `DynamicPropertyRegistrar` 또는 `@DynamicPropertySource` 사용 근거를 코드 주석과 문서에 명시한다.

완료 기준

- Spring Boot 공식 패턴과 일치하는 구조가 된다.
- Oracle/Redis 프로퍼티 주입 방식이 코드와 문서에서 동일하게 설명된다.

### 작업 B-2. Redis 커스텀 프로퍼티 주입 경로 검증

- `jhipster.cache.redis.server`가 실제 테스트 컨텍스트에서 원하는 값으로 주입되는지 확인한다.
- Redisson 초기화가 컨테이너 포트와 정확히 연결되는지 검증 테스트를 추가한다.

완료 기준

- Redis 연결 프로퍼티 검증용 테스트 또는 명시적 로그가 존재한다.

## 작업 스트림 C. 보안 테스트 표준화

### 작업 C-1. `@WithMockUser` 잔존 구간 분류

대상 예시

- `src/test/java/com/daangcool/stack/web/rest/TagAdminResourceIT.java`
- `src/test/java/com/daangcool/stack/web/rest/AccountResourceIT.java`
- 그 외 `src/test/java/` 내 `@WithMockUser` 사용 IT 전반

분류 기준

- 반드시 JWT 기반으로 바꿔야 하는 API 테스트
- 단순 MVC/권한 매핑 검증으로 남겨도 되는 테스트

완료 기준

- JWT 전환 대상 목록이 분리된다.
- 보안 핵심 API는 Bearer 토큰 기반으로 검증된다.

### 작업 C-2. JWT 테스트 헬퍼 표준 예시 문서화

- `JwtAuthenticationTestUtils` 사용 예시를 운영 문서에 추가한다.
- 최소 권한/복수 권한/만료 토큰 등 표준 시나리오를 정리한다.

완료 기준

- 신규 에이전트가 `@WithMockUser`를 습관적으로 추가하지 않도록 기준 문서가 보강된다.

## 작업 스트림 D. 문서 및 로그 정합성 복구

### 작업 D-1. 기존 최종 보고서 정정

- `application-test.yml` 존재 여부 설명 수정
- "전체 119개 테스트 통과" 표현의 근거 재정리
- 실제 삭제/유지 파일 목록 재검토

완료 기준

- 최종 보고서가 현재 저장소 상태와 일치한다.

### 작업 D-2. 테스트 리소스 잔존물 정리

- `src/test/resources/logback.xml`의 레거시 로거 정리 여부 검토
- 더 이상 사용하지 않는 레거시 테스트 설정 참조가 없는지 재점검

완료 기준

- 사용하지 않는 테스트 전용 클래스/로거 참조가 제거되거나 유지 이유가 문서화된다.

## 권장 실행 순서

1. A-1
2. B-1
3. A-2
4. C-1
5. C-2
6. D-1
7. D-2
8. B-2

## 메모

이 작업은 단순 문서 수정이 아니라 테스트 인프라 계약을 다시 정립하는 성격이 강하다.

따라서 후속 구현 시에는 backend agent-log와 필요하면 knowledge item도 함께 남기는 것이 바람직하다.

