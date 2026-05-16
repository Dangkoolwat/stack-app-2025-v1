# 📜 에이전트 작업 이력 (Agent Work History)

이 문서는 Stack App 2025 v1 프로젝트에 참여한 모든 AI 에이전트의 작업 이력을 기록합니다. 완료된 작업에 한해 아래 형식으로 추가하십시오.

**포맷**: `YYYY-MM-DD | 작업명 | 핵심 변경 요약`

---

- 2026-05-16 | legacy-guidance-prune | semble_rs Operation Guide에서 설치/환경설정 중복 설명을 삭제하고 운영 순서만 남김
- 2026-05-16 | stack-app-semble-rs-default-rules | AGENTS.md, workflow, and standards docs에 plan/search/digest 기본값과 Java/Vue deps fallback을 반영
- 2026-05-16 | semble-rs-java-vue-support | AGENTS.md와 semble_rs 운영 문서에 Java/Vue용 `tree --symbols`, `deps`, `search --outline` 흐름과 `impact` caveat를 반영
- 2026-05-16 | agents-md-search-order | AGENTS.md와 연결 정책 문서를 조건형 검색 순서로 정리: 대상 불명확 시 `rg --files` / `rg` 선행, 심볼이 명확하면 Serena 직접, blast radius가 큰 경우에만 `code-review-graph` 추가
- 2026-05-15 | agents-md-hardening | AGENTS.md에 Core Policy Document Defense(2B) 및 High-Risk Integrity Guardrails(11B) 섹션 적용 완료 확인, docs/history.md 신규 생성
- 2026-05-15 | agents-md-portability-routing | AGENTS.md의 절대경로 링크를 repo-relative로 정리하고 docs/config/cross-cutting 작업의 agent-log 라우팅 문구를 보강
- 2026-05-15 | policy-adoption-from-fcpx | fcpx-auto-captions에서 6가지 기법 차용: protocol-design-intent, handshake-protocol, surgical-edit-rules, validation-standard 신규 생성, java-class-comment-guideline 확장(Vue 3/TS 통합), AGENTS.md Section 7 트리거 4행 추가 + Section 11B Anti-Truncation/No Implicit Deletion 추가
- 2026-05-15 | skills-inventory-update | 미사용 스킬(oracle, caveman, sequential-thinking) 3종 삭제 및 핵심 스킬(liquibase-migration, spring-security-oauth2, pinia-state-management) 3종 신규 생성. AGENTS.md Section 4 & 16 에 신규 스킬 트리거 및 superpower 레퍼런스 명시
- 2026-05-15 | agents-md-refinement | AGENTS.md 3대 미비점 보완: Section 7 superpower/karpathy 스킬 트리거 매핑 추가, Section 9 한글 주석(Korean Comment Rule) 글로벌 의무화 명시, Section 11 Frontend 구체적 검증 명령어(npm run build 등) 명시
- 2026-05-15 | agents-md-lazy-loading | AGENTS.md 파일 경량화(Lazy-Loading 아키텍처 완벽 적용): Section 2A(3-Stage Pipeline) 분리, Section 3(Task Levels) 분리, Section 11 및 11B 가드레일을 `validation-standard.md`로 통합. 핵심 도구 가이드 링크 중복(기존 13~15) 제거 및 하위 번호 재정렬(16->13)로 파일 길이 대폭 압축.
- 2026-05-15 | incident-report-template | 에이전트 오작동(False Positive, Zero-Trust 위반 등) 대응을 위한 공식 사고 보고서 템플릿(`docs/operations/incident-reports/_template.md`) 생성 및 `AGENTS.md` Section 7 트리거에 매핑 추가.
- 2026-05-15 | error-recovery-protocol | 에이전트 오판(False Positive) 방지를 위한 2대 방어 조치 추가: 1) `AGENTS.md` Section 6에 물리적 파일 검증 의무(Zero-Trust File Verification) 명문화. 2) `validation-standard.md` 에 오판 발생 시 즉각적인 롤백 및 사고 보고서 작성을 강제하는 오류 복구 절차(Error Recovery Flow) 신설.

---
*마지막 업데이트: 2026-05-16 (v1.5.2)*
