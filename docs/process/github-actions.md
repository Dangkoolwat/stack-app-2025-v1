# GitHub Actions 적용 가이드

## 권장 위치

아래 경로에 저장한다.

```text
.github/workflows/agent-guard.yml
.github/workflows/pr-body-check.yml
```

---

## 역할

### agent-guard.yml
다음을 검사한다.

- 비자명한 변경인데 agent-log가 있는가
- agent-log 필수 파일 6개가 모두 있는가
- 설정/의존성 변경 시 영향/롤백 기록이 있는가
- API 관련 변경 시 OpenAPI 갱신이 필요한지 경고

### pr-body-check.yml
PR 본문에 필수 섹션이 모두 있는지 확인한다.

---

## 주의

- 이 워크플로우는 완전한 의미 분석이 아니라, 실무에서 자주 빠지는 항목을 자동으로 걸러내기 위한 1차 방어선이다.
- 처음에는 경고 위주로 운영하고, 팀이 익숙해지면 실패 조건을 점차 강화하는 것을 권장한다.
- 저장소 구조가 다르면 경로(prefix)와 파일명 규칙을 맞게 수정해야 한다.

---

## 같이 쓰면 좋은 것

- docs/process/git-workflow.md
- docs/process/pr-review-checklist.md
- docs/process/ci-automation-rules.md
- .github/pull_request_template.md
