---
agent: GPT-5 Codex
created_at: 2026-03-28 (Saturday)
language: ko
---

# 구현 계획

## 단계

1. 전역 토큰과 공통 레이아웃 클래스 추가
2. 상단 내비게이션 재구성
3. 로그인 후 홈을 운영형 대시보드로 재설계
4. 게시글/사용자 목록에 공통 헤더/툴바/테이블 푸터 패턴 적용
5. 번역 키 보강
6. 문서화 및 검증

## 변경 파일

- `src/main/webapp/app/app.vue`
- `src/main/webapp/content/scss/global.scss`
- `src/main/webapp/app/core/jhi-navbar/jhi-navbar.vue`
- `src/main/webapp/app/core/jhi-navbar/jhi-navbar.component.ts`
- `src/main/webapp/app/core/home/home.vue`
- `src/main/webapp/app/core/home/home.component.ts`
- `src/main/webapp/app/entities/board/board.vue`
- `src/main/webapp/app/admin/user-management/user-management.vue`
- `src/main/webapp/app/admin/user-management/user-management.component.ts`
- `src/main/webapp/i18n/ko/global.json`
- `src/main/webapp/i18n/en/global.json`
- `src/main/webapp/i18n/ko/home.json`
- `src/main/webapp/i18n/en/home.json`

## 테스트

- `npx eslint` for changed Vue/TS files
- `npm run webapp:build:dev`
