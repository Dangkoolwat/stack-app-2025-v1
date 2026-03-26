---
agent: Antigravity (Gemini)
created_at: 2026-03-26 (수)
language: ko
---

# 가이드 문서 전체 감사 및 수정

## 요약

프로젝트 전체 19개 가이드 문서를 감사하여 12건의 문제를 식별하고 전부 수정했다.
핵심 추가 사항은 AGENTS.md에 Consistency Sweep Rule 섹션으로, 에이전트가 cross-cutting 변경 시 부분 적용을 방지하는 구체적 IF-THEN 규칙과 검증 명령어를 포함한다.

## 이유

에이전트가 CacheNames 상수 클래스 도입, ResourceAuthorizationService 적용 등의 cross-cutting 변경을 하나의 서비스에만 적용하고 나머지는 방치하는 패턴이 반복되었다. 기존 AGENTS.md의 Global Impact Review 섹션은 "affected systems를 확인하라"는 추상적 지시만 있었고, 구체적 검증 방법이 없었다.

## 변경 파일

수정:
- AGENTS.md (Consistency Sweep Rule 추가, Global Impact Review 강화, Self-Check 항목 추가, KI 검색 규칙 추가)
- docs/README.md (문서 구조에 analysis, knowledge, release-notes 추가)
- docs/workflow/git-workflow.md (커밋 형식 통일, agent-log 중복 제거)
- docs/standards/configuration-externalization-guideline.md (중복 보안 규칙 제거, 참조로 교체)
- docs/analysis/2026-03-22-system-report/system-architecture-security-report.md (이모지 제거)

삭제:
- docs/standards/process-improvement.md (마크다운 깨짐, 내용 이미 반영됨)
- docs/standards/documentation-structure-standard.md (docs/README.md와 중복)

이동:
- docs/operations/system-report.md -> docs/analysis/2026-03-22-system-report/
- docs/operations/system-architecture-security-report.md -> docs/analysis/2026-03-22-system-report/

## 영향

- AGENTS.md 307줄 -> 354줄 (Consistency Sweep Rule +47줄)
- 소스 코드 변경 없음
- API 계약 변경 없음
- 보안 영향 없음

## 검증

- 삭제/이동된 파일에 대한 stale 참조 grep 검색: 0건
- 분석 리포트 이모지 grep 검색: 0건
