---
agent: Codex
created_at: 2026-05-16
language: ko
---

# 제안

AGENTS.md의 검색/탐색 우선순위를 조건형으로 정리합니다.

- 대상이 불명확하면 `rg --files` / `rg`를 먼저 사용합니다.
- 대상이 이미 심볼 단위로 보이면 Serena를 직접 사용합니다.
- blast radius가 크거나 불명확할 때만 `code-review-graph`를 추가합니다.
- 같은 의미가 `docs/workflow/`와 `docs/standards/`에도 맞게 반영되도록 맞춥니다.
