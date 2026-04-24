---
agent: Antigravity
created_at: 2026-03-28 (토요일)
language: ko
---

# 해결 방안 제안

## 제안 1. 종합적인 UI/UX 유효성 검사 및 라우터 정규화 (권장)

- **게시판 생성 유효성**: `BoardUpdate.component.ts`의 `save()` 메서드에서 `Vuelidate` 또는 조건문으로 `title`, `content`, `boardTypeCode` 필수 값을 체크합니다. 제목/내용이 비었거나 유형 미선택 시 토스트 경고 메시지를 표시합니다.
- **백엔드 정합성**: `BoardResource.createBoard`에서 서버 측 검증을 추가하여 `BadRequestAlertException`을 반환합니다.
- **연결 오류 해결**: `vite.config.ts`의 프록시 설정을 `localhost` 대신 `127.0.0.1`로 명시적으로 변경하여 매핑 불일치를 해결합니다 (IPv6 관련 이슈).
- **라우터/등록 오류**: `/account/settings` 및 `/admin/logs`의 컴포넌트 로딩 방식을 점검하고 파일 경로 및 기본 내보내기(default export) 정합성을 맞춥니다.
- **WebSocket 인증**: `TrackerService`와 시큐리티 설정을 업데이트하여 JWT 인증을 강화합니다.

## 제안 2. 최소한의 백엔드 필터링 및 글로벌 컴포넌트 등록

- **유효성**: 백엔드 검증에만 의존하고 프론트엔드는 단순 차단만 수행합니다.
- **라우터 고립**: 개별 라우터 모듈(`account.ts`, `admin.ts`) 대신 `main.ts`에서 컴포넌트를 직접 로드하여 등록 오류를 회피합니다.

---

## 제안 및 확인 요청

권장 방안(제안 1)을 통해 게시판 유효성 검사 및 404/연결 오류를 통합적으로 해결하고자 하오니, **이 방향으로 진행해도 될지 확인 부탁드립니다.**
确认을 해주시면 상세 구현 계획(Implementation Plan)에 따라 작업을 시작하겠습니다.
