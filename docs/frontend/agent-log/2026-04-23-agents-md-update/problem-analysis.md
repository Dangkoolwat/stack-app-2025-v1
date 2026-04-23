---
agent: Gemini 3 Flash
created_at: 2026-04-23 (목요일)
language: ko
---

# 문제 분석

## 개요
사용자는 `AGENTS.md` 파일에 `graphify` 도구의 그래프를 최신화하는 구체적인 명령어를 추가할 것을 요청하였습니다. 이는 코드 구조에 중대한 변화가 생겼을 때 아키텍처 가시성을 유지하기 위한 조치입니다.

## 상세 분석
1. **대상 파일**: `AGENTS.md`
2. **변경 지점**: `## Supplemental Architecture Analysis (Graphify)` 섹션의 `### 2. Usage Guidelines`.
3. **기존 내용**: `Synchronization` 항목에서 `updateGraphify` 명령어를 언급하고 있으나, 구체적인 쉘 명령어(`graphify update . && mv graphify-out/* docs/graphify/`)가 명시되어 있지 않음.
4. **제약 사항**:
    - `AGENTS.md` 내의 공통 문서는 영어로 작성되어야 함.
    - 볼딩(`**`) 및 이모지 사용 금지 (Formatting Policy).
    - 시니어 아키텍트 페르소나 및 Surgical Precision 준수.

## 영향 범위
- `AGENTS.md`를 참고하는 모든 에이전트의 행동 지침에 영향을 미침.
- 코드 로직에는 직접적인 영향이 없는 문서 수정 작업임.
