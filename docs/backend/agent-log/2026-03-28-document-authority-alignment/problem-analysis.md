---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# Problem Analysis

문서 수 자체가 문제라기보다, 서로 다른 성격의 문서가 모두 비슷한 톤으로 작성되어 에이전트가 어떤 문서를 규칙으로 따라야 하는지 혼동할 가능성이 있었다.

특히 `docs/backend/Architecture.md`, `docs/backend/Engineering_Guideline.md`, `docs/frontend/Architecture.md`, `docs/frontend/Engineering_Guideline.md`는 제목과 표현만 보면 표준 문서처럼 보이지만, 실제 우선순위 체계와의 관계가 분명하게 적혀 있지 않았다.

또한 `docs/knowledge/`와 `agent-log`는 재사용 가치가 높지만, 기준 문서가 아닌데도 세부 내용이 강하게 쓰여 있으면 사실상 정책처럼 오해될 수 있다.
