---
agent: Gemini 3 Flash
created_at: 2026-04-23 (목요일)
language: ko
---

# 구현 계획

## 단계별 계획
1. **AGENTS.md 수정**: line 209의 `Synchronization` 항목을 새로운 명령어로 교체.
2. **검증**: `cat` 명령어로 수정된 라인 확인.

## 변경 대상 파일
- `AGENTS.md` (line 209)

## 상세 변경 내용
```markdown
- Update: If there is a significant change in the code structure, maintain the graphs in docs/graphify in the latest state by running the command: graphify update . && rm -rf docs/graphify/* && mv graphify-out/* docs/graphify/ && rm -rf graphify-out
```
