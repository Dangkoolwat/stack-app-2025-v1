---
agent: Antigravity (Gemini 2.0 Flash)
created_at: 2026-03-25 (수요일)
language: ko
---

# 문제 분석

## 문제 현상
영어 모드에서 `entities.commonCodeDetail.messages.selectGroup` 문구가 정상적으로 표시되지 않고 원본 키 값이 노출됨.

## 원인 분석
- `src/main/webapp/i18n/ko/common-code-detail.json`에는 해당 키가 정의되어 있으나, `src/main/webapp/i18n/en/common-code-detail.json`에는 누락되어 있음.
- UI 컴포넌트(`common-code-detail.vue`)에서 해당 메시지 키를 참조하고 있으나 번역 리소스가 제공되지 않아 발생함.

## 영향 범위
- 영어 사용자의 공통 코드 상세 관리 화면 (그룹 선택 시 안내 메시지)
