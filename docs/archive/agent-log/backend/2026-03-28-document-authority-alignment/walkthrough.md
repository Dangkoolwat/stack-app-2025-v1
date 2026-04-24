---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# Walkthrough

먼저 `docs/README.md`, `AGENTS.md`, backend/frontend 보조 가이드를 함께 읽어 우선순위 체계와 실제 문서 배치가 어떻게 보이는지 확인했다.

그 결과, 기준 체계는 이미 존재하지만 보조 문서들이 자신이 "참고 문서"라는 사실을 문서 상단에서 바로 드러내지 않아 오해 소지가 있다는 점을 확인했다.

그래서 해결 방향을 두 층으로 나눴다.

1. 기준 문서 강화
- `docs/README.md`에 authority matrix를 추가해 각 폴더가 규칙, 절차, 참고, 이력 중 무엇인지 바로 알 수 있게 했다.
- `AGENTS.md`에도 backend/frontend 문서, knowledge, agent-log의 지위를 명시했다.

2. 보조 문서 정리
- backend/frontend 보조 문서 상단에 reference-only 역할 설명을 추가했다.
- 문서 제목과 문구를 정리해 "표준 문서처럼 보이는" 인상을 줄였다.
- 재사용 가능한 교훈은 `docs/knowledge/`로 분리했다.

마지막으로 `docs/` 하위 `.DS_Store` 파일을 제거해 불필요한 노이즈도 정리했다.
