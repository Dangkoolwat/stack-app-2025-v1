---
agent: Antigravity (Gemini 2.0 Flash)
created_at: 2026-03-25 (수요일)
language: ko
---

# 워크쓰루 (Walkthrough)

## 로직 상세 설명
1. `ko/common-code-detail.json`과 `en/common-code-detail.json` 파일의 `messages` 세션을 비교하여 누락된 키(`selectGroup`)를 식별했습니다.
2. 영어 리소스 파일에 해당 키를 추가하여 i18n 엔진이 런타임에 올바른 문구를 찾을 수 있도록 조치했습니다.
3. 관련 Vue 파일(`common-code-detail.vue`)의 `t$()` 호출부를 확인하여 키 명칭이 일치함을 재검증했습니다.

## 핵심 포인트
- i18n 키의 일관성 유지.
- 사용자 인터페이스에서의 가독성을 고려한 번역어 선택.
