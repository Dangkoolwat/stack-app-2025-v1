# 2026-03-17 Agent 규칙(.cursorrules) 및 로그 템플릿 정합성 개선

## 기본 정보

- **Date:** 2026-03-17
- **Agent:** Codex (GPT-5.2)
- **Task Title:** Cursor 규칙 + agent-log 템플릿 정리
- **Goal:** `AGENTS.md`의 요구사항이 Cursor 환경에서도 누락 없이 적용되도록 `.cursorrules`를 보강하고, `docs/agent-log/template.md`의 예시를 본 백엔드(Maven/Spring Boot) 기준으로 정합화한다.

## Context

- 레포에는 `AGENTS.md`(프로젝트 규칙)와 Cursor용 `.cursorrules`(요약 규칙), `docs/agent-log/template.md`(로그 템플릿)가 공존.
- `.cursorrules`는 기본 규칙(로그/아키텍처/보안/검증)은 포함했지만, API 계약/DB(Liquibase) 영향 체크 등 일부 항목이 빠져 있었고, 템플릿의 Verification 예시가 Node 중심이라 백엔드 작업에 부적합했음.

## Work Performed

1. `.cursorrules`에 영향 범위 사전 점검(레이어/API/보안/DB) 및 관련 로그 확인 항목을 추가.
2. `.cursorrules`에 API 계약/퍼시스턴스(Liquibase)/보안 항목을 명확히 추가.
3. `docs/agent-log/template.md`의 Verification 예시를 `./mvnw test/verify/clean package` 중심으로 변경.

## Files Modified

- `.cursorrules`
- `docs/agent-log/template.md`

## Architecture Impact

No architectural changes.

## Security Impact

No security impact.

## Verification

- 문서/규칙 변경으로 빌드 동작 영향은 없으나, 변경 파일들의 내용/정합성 수동 확인 수행.

## Risks

- No significant risks identified.

## Next Suggested Tasks

- 필요 시 `AGENTS.md`의 규칙 변경이 있을 때 `.cursorrules`/`template.md`도 함께 업데이트하는 운영 규칙을 팀 합의로 추가.

## Notes for Future Agents

- Cursor 사용 시 `.cursorrules`는 최소 준수 규칙이므로, 상세 규칙/예외는 항상 `AGENTS.md`를 최종 기준으로 삼을 것.

