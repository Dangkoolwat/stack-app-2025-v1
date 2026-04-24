---
agent: Gemini 3 Flash
created_at: 2026-03-28 (Sat)
language: ko
---

# 구현 계획 (Implementation Plan)

## 개요
에러 페이지의 사용자 경험을 개선하고 i18n 리소스의 정합성을 확보하기 위한 작업 계획입니다.

## 변경 내용

### i18n 영역
-   `ko/error.json`, `en/error.json`에 404 전용 키 추가
-   기존 소스 코드 내 i18n 키(t$()) 전수 조사 및 누락 건 수정

### UI 컴포넌트 영역
-   `error.vue`: 중앙 정렬된 프리미엄 레이아웃 구현
-   홈으로 가기 버튼 추가

### 스타일 영역
-   `global.scss`: 에러 페이지 전용 CSS 클래스 정의

## 검증 계획
-   존재하지 않는 URL로 접속하여 레이아웃 및 다국어 텍스트 노출 확인
