---
agent: Antigravity
created_at: 2026-03-28 (토요일)
language: ko
---

# 자기 점검 (Self-Check)

## 1. 정합성 (Correctness)
- [ ] 제목/내용/게시판 유형 필수 값 검증 로직이 프론트엔드와 백엔드 모두 적용되는가?
- [ ] `/account/settings` 및 `/admin/logs` 경로가 더 이상 404를 반환하지 않는가?
- [ ] 드롭다운 목록 등 공통 코드 데이터를 백엔드에서 정상적으로 불러오는가?

## 2. 보안 (Safety)
- [ ] 백엔드 `BoardResource`에서 사용자 권한 및 데이터 유효성을 검증하는가?
- [ ] WebSocket 인증(Tracker) 시 JWT 토큰이 안전하게 처리되는가?
- [ ] `/management/**` 경로가 외부에 노출되지 않도록 설정이 유지되는가?

## 3. 이해 가능성 (Understandability)
- [ ] 코드 주석이 한국어로 작성되었는가?
- [ ] `board.json` 및 `error.json`에 새로운 메시지 키가 표준에 맞게 추가되었는가?
- [ ] 변경 사항이 `agent-log` 및 `walkthrough.md`에 충실히 기록되었는가?

## 4. 아키텍처 (Architecture)
- [ ] JHipster 9 및 Spring Boot 4 표준에 부합하는 방식으로 유효성 검사 및 라우팅이 구현되었는가?
- [ ] `/api` 프록시 경로가 IPv6/IPv4 환경에서 모두 호환되도록 구성되었는가?
