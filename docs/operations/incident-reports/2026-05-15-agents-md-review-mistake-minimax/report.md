# 🚨 AI Agent Incident Report
**문서 번호 (Incident ID):** IR-20260515-01
**작성 일자 (Date):** 2026-05-15
**보고자 (Reporter):** AI Agent
**관련 에이전트 (Involved Agent):** minimaxai/minimax-m2.7
**사고 유형 (Category):** False Positive

---

## 1. 사고 개요 (Incident Summary)
* **이슈 명칭 (Title):** AGENTS.md 리뷰 중 참조 파일 존재 여부에 대한 잘못된 버그 판단
* **발생 상황 (Context & Trigger):**
  * AGENTS.md 파일 리뷰를 수행 중
  * Policy Trigger Table에 참조되는 파일들(`semble-troubleshooting.md`, `handshake-protocol.md`, `surgical-edit-rules.md`)의 존재 여부에 대해 "참조 누락 위험" 이라고 지적
  * 근거: `glob` 툴 결과가 100개에서 잘려서 전체를 보지 못함
* **최종 결과 (Impact):**
  * 잘못된 버그 보고서 생성 후 삭제
  * 추측을 사실로 오인하여 불필요한 보고서 작성

---

## 2. 근본 원인 분석 (Root Cause Analysis)
1. **"Read Before Write" 원칙 위반:** 실제 파일 존재 여부를 확인하지 않고 추측으로 판단
2. **"Zero Context Contamination" 위반:** 검증되지 않은 정보를 사실인 것처럼 전달
3. **기술적 한계:** `glob` 툴 결과가 100개에서 잘리는 경우를 고려하지 않음
4. **추측과 사실의 혼동:** "미확인"은 추측이지 사실이 아님을 인식하지 못함

---

## 3. 검증 및 복구 조치 (Validation & Resolution)
1. **물리적 검증 (Physical Verification):**
   * `bash ls` 명령으로 `docs/standards/` 디렉토리 내 해당 파일들 직접 확인
   * 결과: 모든 참조 파일 정상 존재 확인
2. **복구/방어 조치 (Remediation):**
   * 기존 잘못된 경로의 보고서 삭제
   * 템플릿에 맞추어 `docs/operations/incident-reports/` 폴더에 재작성

---

## 4. 향후 대책 (Action Items & Preventative Measures)
* [ ] 파일 존재 여부는 반드시 `bash` (ls/test) 로 직접 확인 후 판단
* [ ] "미확인"은 추측임을 인식하고, 검증 절차 필요성을 명시
* [ ] glob/list_dir 결과가 잘린 경우 추가 검증 수행
* [ ] 추측을 사실처럼 보고하지 않도록 주의