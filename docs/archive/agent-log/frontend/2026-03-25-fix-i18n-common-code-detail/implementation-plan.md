---
agent: Antigravity (Gemini 2.0 Flash)
created_at: 2026-03-25 (수요일)
language: ko
---

# 구현 계획

## 1단계: 코드 수정
- `src/main/webapp/i18n/en/common-code-detail.json` 수정.
- `messages` 객체 내부에 `"selectGroup": "Please select a common code group."` 추가.

## 2단계: 검증
- 해당 파일의 JSON 문법 유효성 확인.
- (수동) 브라우저에서 영어 모드로 전환 후 문구 노출 확인.
