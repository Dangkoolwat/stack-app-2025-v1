# Walkthrough: Successful Guideline Patch for Gemini 3.5 Flash & 3.1 Pro

## 📊 작업 요약
- **수행 모델**: Gemini 3.5 Flash (High)
- **작업 상태**: 수정 및 패치 완료
- **영향 파일**:
  1. `AGENTS.md` (MODIFIED)
  2. `docs/workflow/agent-workflow-pipeline.md` (MODIFIED)
  3. `docs/history.md` (MODIFIED)
- **검증 여부**: 
  - `sync-docs-routing-cache.sh` 실행 결과: 성공 (Exit Code 0)

---

## 1. 개정 상세 내역

### A. [AGENTS.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/AGENTS.md)
- **Section 2D 신설**: `Model-Specific Execution Guidelines (Gemini 3.5 Flash & 3.1 Pro)`
- **Gemini 3.5 Flash 룰셋**: Fast Track 작업에 최적화, 500라인 이상 파일의 통째 읽기 금지(Context Guard), 초고속 멀티 툴 체이닝 적극 활용.
- **Gemini 3.1 Pro 룰셋**: Standard Planning 작업 및 High-Risk 변경 제어, BPI 프로세스 강제, `code-review-graph` 활용한 충격 반경 분석 선행.

### B. [agent-workflow-pipeline.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/workflow/agent-workflow-pipeline.md)
- **Efficiency Constraints 섹션 확장**:
  - `Context Economy Limit`: 500라인 초과 파일 전체 읽기 금지 조항 공식 삽입.
  - `Model-Specific Discovery (Two-Speed Tooling)`: 모델별 이중 속도 도구 사용 흐름 명문화 (3.5 Flash: 빠른 중첩 툴 체이닝 / 3.1 Pro: `semble_rs plan` 및 `code-review-graph` 필수 우선 활용).

### C. [docs/history.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/history.md)
- **에이전트 이력 갱신**: 신규 가이드라인 최적화 이력 기입 및 최종 버전 갱신 완료 (`v1.6.0`).

---

## 2. 검증 결과 및 안전 장치
- **문서 동기화**: `sh scripts/sync-docs-routing-cache.sh`가 정상적으로 가동되어 메타데이터 캐시 및 폴더 구조 정렬이 올바르게 리프레시되었음.
- **Zero Trust 및 Surgical Edit**: 레거시 텍스트의 훼손 없이 정확히 대상 섹션에만 코드가 안전하게 패치되었음을 크로스 체크함.
