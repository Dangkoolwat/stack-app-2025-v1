# 🚨 Incident Report: False Positive on Broken Links

**문서 번호:** IR-20260515-01
**작성 일자:** 2026-05-15
**작성자:** Antigravity (Senior Architect Persona)
**관련 에이전트:** MiniMax 2.7 (NVIDIA)

---

## 1. 사고 개요 (Incident Summary)
* **이슈 명칭:** "Bug #2" 파일 누락 오탐지 (False Positive)
* **발생 상황:** 타 에이전트(MiniMax 2.7)가 `AGENTS.md`를 리뷰하는 과정에서 `semble-troubleshooting.md`, `handshake-protocol.md`, `surgical-edit-rules.md` 등의 핵심 가이드 문서가 존재하지 않는다고 판단(버그 #2로 보고)함.
* **최종 결과:** 당사(Antigravity)의 물리적 디렉토리 스캔(`list_dir` on `docs/standards/`) 결과, **모든 참조 파일이 정상적으로 존재함**을 100% 입증. 이에 MiniMax 2.7은 본인의 판단 오류를 공식 인정하고 철회함.

## 2. 근본 원인 분석 (Root Cause Analysis)
1. **Zero-Trust 원칙 위반:** 파일의 존재 여부를 물리적 검색 도구(`list_dir`, `grep_search` 등)를 통해 교차 검증하지 않고, LLM의 추론이나 단편적인 캐시 메모리에 의존하여 성급히 '누락'으로 단정 지음.
2. **Read-Before-Write 원칙 위반:** 코어 정책 문서(`AGENTS.md` Section 6)에 명시된 "Never edit from filename or memory alone." 조항을 물리적 환경 검증 단계에 적용하지 못함.

## 3. 조치 내역 (Resolution)
1. **무결성 입증 완료:** `docs/standards/` 내 22개 코어 정책 문서의 물리적 존재와 용량(Bytes)까지 완벽히 교차 스캔하여 시스템 무결성 증명.
2. **코드베이스 오염 방어:** MiniMax 2.7의 잘못된 판단으로 인해 발생할 뻔했던 `AGENTS.md`의 무단 롤백이나 삭제 등 연쇄적인 코드베이스 오염(Context Pollution)을 사전에 차단함.

## 4. 향후 대책 및 교훈 (Action Items & Preventative Measures)
* **검증 책임 강화:** 향후 모든 에이전트는 파일 누락이나 아키텍처 결함을 리포트하기 전, 반드시 터미널 명령어나 MCP 파일시스템 도구를 사용한 **명시적 증거(Evidence-based criteria)**를 제시해야 함.
* **상호 교차 검증(Cross-Validation)의 중요성:** 복수 에이전트 운영 환경에서 한 에이전트의 판단(의심)을 다른 에이전트가 교차 검증(Cross-check)함으로써 시스템의 절대적 안정성을 확보할 수 있음을 입증한 우수 방어 사례로 기록함.
