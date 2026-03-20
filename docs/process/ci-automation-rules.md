# CI / Automation Rules

## 목적
이 문서는 사람이 직접 확인하지 않아도 되는 항목을 자동으로 검증하기 위한 CI 및 자동화 규칙 초안을 정의한다.

---

## 자동 검증 대상

사람이 굳이 직접 보지 않아도 되는 항목은 자동화 대상으로 본다.

예:
- agent-log 존재 여부
- 필수 파일 존재 여부
- OpenAPI 파일 존재 여부
- 설정 파일 변경 시 impact 문서 존재 여부
- PR 템플릿 필수 항목 누락 여부

---

## 1차 자동화 대상 (우선 적용 권장)

### agent-log 검사
아래를 자동 확인한다.

- 비자명한 변경인데 agent-log가 존재하는가
- 필수 파일 6개가 모두 존재하는가

필수 파일:
- problem-analysis.md
- proposal.md
- self-check.md
- implementation-plan.md
- walkthrough.md
- final-report.md

---

### OpenAPI 검사
아래를 자동 확인한다.

- API 관련 코드 변경 시 OpenAPI 파일도 함께 변경되었는가
- Swagger에서 읽을 수 있는 형식이 유지되는가

---

### Config / Dependency 변경 검사
아래를 자동 확인한다.

- application.yml / .env / build.gradle / pom.xml / package.json 변경 시
  - 영향 분석 문서가 존재하는가
  - final-report.md에 관련 내용이 있는가

---

## 2차 자동화 대상 (확장 시 권장)

### 코드 주석 검사
고위험 영역 변경 시 변경 이력 주석이 있는지 검사할 수 있다.

예상 대상:
- config/
- security/
- cache/
- infra/

주의:
- 완전 자동 판별은 어려우므로, 강제보다는 경고 수준으로 시작하는 것이 좋다.

---

### PR 본문 검사
PR 템플릿 필수 항목이 비어 있는지 검사할 수 있다.

예:
- Problem 미작성
- Impact Scope 미작성
- Test 미작성
- Rollback Plan 미작성

---

## CI 실패 기준 예시

아래 조건이면 CI를 실패시킬 수 있다.

- agent-log 필수 파일 누락
- API 변경인데 OpenAPI 누락
- Config 변경인데 영향 분석 누락
- PR 템플릿 핵심 항목 누락

---

## 점진 적용 원칙

처음부터 모든 규칙을 강하게 실패 처리하지 않는다.

권장 순서:
1. 경고 출력
2. 중요 항목만 실패 처리
3. 안정화 후 고위험 변경 범위까지 확대

---

## 권장 자동화 순서

### 1단계
- agent-log 존재 확인
- 필수 파일 6개 확인

### 2단계
- OpenAPI 변경 확인
- Config/Dependency 변경 시 impact 문서 확인

### 3단계
- PR 템플릿 필수 항목 확인
- 고위험 영역 주석/문서 경고 추가

---

## 운영 원칙

자동화의 목적은 리뷰어를 대체하는 것이 아니라, 리뷰어가 직접 보면 안 되는 단순 검사를 대신하는 것이다.

원칙:
- 사람은 판단한다.
- CI는 누락을 잡는다.
- 고위험 변경만 사람이 깊게 본다.
