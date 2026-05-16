---
agent: Codex
created_at: 2026-05-16
language: ko
---

# 최종 보고

## 요약
`semble_rs`의 AST-aware 발견 흐름을 Java/Vue 기준으로 다시 정렬하고, `AGENTS.md`와 Semble 운영 문서를 같은 의미로 맞췄습니다.

## 변경 결과
- `tree --symbols`를 Java/Vue 구조 맵 용도로 허용
- `deps`를 파일의 직접 import / defined symbol 확인 용도로 허용
- `search --outline`을 더 작은 signature-only 후보 확인 용도로 허용
- `impact`는 빠른 reverse-dependency probe로만 취급, 빈 결과는 inconclusive로 명시
- `AGENTS.md`의 검색 순서 안내를 Semble 새 흐름과 맞춤

## 검증
- `semble_rs tree --symbols` 완료
- `semble_rs deps` 완료
- `semble_rs impact` 완료
- `git diff --check` 완료

## 남은 리스크
- 기존 작업자나 캐시된 에이전트 안내문이 이전 텍스트를 계속 볼 수 있으므로, 새 문구를 읽도록 다시 로드가 필요할 수 있습니다.
