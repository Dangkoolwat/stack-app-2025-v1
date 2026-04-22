---
agent: Gemini 3 Flash
created_at: 2026-04-22 (수요일)
language: ko
---

# 구현 흐름

## 1. 글로벌 스킬 디렉토리 확인 및 생성
- 최종 글로벌 저장소 위치 확인: `/Users/sanghyoukjin/.agents/skills`

## 2. 불필요 스킬 이동 (Migration)
- 다음 5개 스킬을 로컬 프로젝트에서 글로벌 공용 저장소로 이동함.
    - `shadcn`
    - `shadcn-ui`
    - `openai-docs`
    - `readme-i18n`
    - `whisper-transcription`

## 3. 이동 결과 검증
- 홈 디렉토리 내 공용 스킬 저장소(`.agents/skills`)에 정상적으로 배치되었는지 확인.

# 핵심 포인트
- 프로젝트 스택(Bootstrap/Sass)과 충돌하는 `shadcn`을 제거하여 에이전트의 제안 품질을 개선함.
- 글로벌 저장소로 이동함으로써 다른 프로젝트에서 필요시 언제든 다시 사용할 수 있는 상태로 보존함.
