---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 테스트 규칙 오해 가능성 검토

## 검토 대상

- `AGENTS.md`
- `docs/operations/testing-guideline.md`
- `pom.xml`

## 결론

현재 문서들은 테스트 표준의 방향성은 잘 제시하지만, 실제 Maven 실행 계약과 1:1로 맞물리지 않는 표현이 있어 다른 에이전트가 오해할 가능성이 있다.

## 오해 가능성이 있는 지점

### 1. AGENTS의 테스트 명령 예시와 실제 IT 실행 경로 차이

`AGENTS.md`의 Self-Check 섹션은 다음 명령을 추천한다.

- `export $(xargs < .env) && ./mvnw clean test`

하지만 현재 `pom.xml`의 surefire 설정은 `**/*IT*`를 제외한다.

따라서 이 명령은 통합 테스트 전체 통과를 보장하지 않을 수 있다.

오해 위험

- 에이전트가 `clean test`만 수행하고도 "Integration tests passing"으로 체크할 수 있음
- 실제로는 IT가 실행되지 않았는데 완료 보고에 통합 테스트 통과를 적을 수 있음

권장 보완

- `AGENTS.md`에 `test`와 `verify`의 역할 차이를 명시
- IT를 별도 실행하는 명령 또는 profile을 명시

### 2. `@WithMockUser` 금지 수준의 표현 차이

`AGENTS.md`는 stateless JWT 환경에서 `@WithMockUser`를 사용하지 말라고 강하게 규정한다.
`docs/operations/testing-guideline.md`도 통합 테스트에서 토큰 기반 인증을 사용하라고 안내한다.

하지만 저장소의 실제 IT 다수는 아직 `@WithMockUser`를 사용한다.

오해 위험

- 신규 에이전트는 규칙을 보고 기존 테스트도 이미 토큰 기반으로 정리되었다고 오해할 수 있음
- 반대로 기존 테스트를 본 에이전트는 규칙을 권고 수준으로 오해할 수 있음

권장 보완

- "기존 테스트 중 전환 미완료 구간이 있다"는 전환 상태 문서 추가
- 보안 핵심 IT와 단순 권한 매핑 테스트를 구분하는 기준 문서화

### 3. `application-test.yml` 기준 표현과 실제 파일 구조 차이

운영 가이드는 test profile 설명을 주고 있으나 실제 저장소는 `application-testdev.yml`, `application-testprod.yml` 중심으로 구성되어 있다.

오해 위험

- 에이전트가 존재하지 않는 `application-test.yml`을 기준으로 수정 시도
- rate limit 비활성화 위치를 잘못 찾을 수 있음

권장 보완

- 운영 가이드에 실제 사용 중인 테스트 설정 파일명을 명시
- 프로파일 조합 예시를 `testdev + test` 기준으로 정리

### 4. 테스트 수치 보고 방식 부재

현재 문서에는 "119개 통과"처럼 정량 수치가 등장하지만, 어떤 Maven goal과 어떤 리포트를 근거로 삼는지 표준이 없다.

오해 위험

- 서로 다른 명령 결과를 같은 기준처럼 보고할 수 있음
- unit test 통과와 integration test 통과가 섞여 기록될 수 있음

권장 보완

- agent-log final-report에 테스트 명령과 리포트 위치를 함께 남기도록 규칙 추가

## 제안 문구

아래 수준의 문구가 AGENTS 또는 운영 가이드에 추가되면 혼동을 크게 줄일 수 있다.

1. `./mvnw test`는 unit 및 일부 non-IT 테스트 검증용이다.
2. `*IT` 검증은 별도 실행 경로를 사용하며, 완료 보고 시 해당 명령과 리포트 위치를 함께 남겨야 한다.
3. stateless JWT 환경의 신규 또는 수정 대상 보안 통합 테스트는 `JwtAuthenticationTestUtils` 기반 Bearer 토큰 방식을 기본으로 한다.
4. 기존 `@WithMockUser` 기반 테스트는 전환 대상 여부를 확인한 뒤 유지 또는 교체한다.

## 판단

문서 자체가 잘못된 것은 아니다.

문제는 문서가 설명하는 이상적인 표준과 현재 저장소의 과도기 상태가 분리되어 기록되지 않았다는 점이다.

이 과도기 상태만 명시되면, 다른 에이전트가 규칙을 오해할 가능성은 크게 줄어든다.

