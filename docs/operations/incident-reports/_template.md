<!-- 
[저장 규칙 (Save Protocol)]
에이전트는 사고 보고서 작성 시 반드시 아래 규칙을 따라 전용 폴더를 생성하고 저장해야 합니다.
1. 저장 위치: `docs/operations/incident-reports/[YYYY-MM-DD]-[TaskName]-[ModelName]/`
2. 파일명: `report.md`
(예시: docs/operations/incident-reports/2026-05-15-agents-md-review-mistake-minimax/report.md)
-->

# 🚨 AI Agent Incident Report Template
**문서 번호 (Incident ID):** IR-YYYYMMDD-XX
**작성 일자 (Date):** YYYY-MM-DD
**보고자 (Reporter):** [에이전트 페르소나 또는 사용자 이름]
**관련 에이전트 (Involved Agent):** [사고를 발생시켰거나 연관된 에이전트 모델명]
**사고 유형 (Category):** [False Positive / Context Loss / Destructive Action / Policy Violation 등]

---

## 1. 사고 개요 (Incident Summary)
* **이슈 명칭 (Title):** [사건을 한 줄로 요약]
* **발생 상황 (Context & Trigger):**
  * 어떤 작업을 수행 중이었는가?
  * 어떤 도구(Tool) 또는 프롬프트가 트리거되었는가?
* **최종 결과 (Impact):**
  * 코드베이스 오염 여부, 정책 훼손 여부, 잘못된 정보 리포트 여부 등 기재.

## 2. 근본 원인 분석 (Root Cause Analysis)
*사고가 발생한 원인을 기술합니다. 가급적 AGENTS.md 내 규칙 위반 사항과 매핑하십시오.*
1. **[위반 정책 명칭] 위반:** (예: Zero-Trust 원칙, Read-Before-Write 원칙 등)
2. **기술적 한계/오류:** (예: 파일 시스템 접근 도구 미사용, 환각(Hallucination), 컨텍스트 윈도우 초과 등)

## 3. 검증 및 복구 조치 (Validation & Resolution)
*사고 수습을 위해 취해진 물리적 검증 및 복구 단계를 기술합니다.*
1. **물리적 검증 (Physical Verification):** (예: `list_dir` 도구를 통한 파일 존재 유무 크로스 체크)
2. **복구/방어 조치 (Remediation):** (예: 잘못된 제안 거절, Atomic Rollback 수행 등)

## 4. 향후 대책 (Action Items & Preventative Measures)
*동일한 사고의 재발을 막기 위해 가이드라인 문서나 프롬프트를 어떻게 수정해야 하는지 제안합니다.*
* [ ] (예시) 파일 시스템 관련 이슈 제기 전 필수적으로 증거(Evidence-based) 수집 과정 강제화
* [ ] (예시) AGENTS.md의 특정 조항 업데이트 필요성 검토
