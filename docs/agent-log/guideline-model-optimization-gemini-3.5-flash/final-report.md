# Final Report: Detailed Guideline Verification & Optimization for Gemini 3.5 Flash & 3.1 Pro

## 📊 작업 요약
- **수행 모델**: Gemini 3.5 Flash (High)
- **작업 상태**: 점검 및 분석 완료
- **영향 파일**: `AGENTS.md`, `docs/workflow/agent-workflow-pipeline.md`
- **검증 여부**: 로컬 구조 분석 및 설계 완료

---

## 1. 개요 및 배경
Antigravity 에이전트의 물리적 추론 엔진이 **Gemini 3.5 Flash** 및 **Gemini 3.1 Pro**로 변경됨에 따라, 가이드라인의 전면적인 점검과 미비점 보완이 요구됩니다. 
본 보고서는 JHipster 기반 Spring Boot 3 + Vue 3 Monorepo 프로젝트 환경에서의 상세한 가이드라인 점검 결과와 식별된 미비점, 그리고 이를 최적화하기 위한 구체적인 아키텍처적 추천 사항을 제시합니다.

---

## 2. 상세 가이드라인 점검 결과 (Checklist & Gaps)

### [점검 1] `AGENTS.md` (Repository Core Policy)
- **현황**: 에이전트의 전역적 행동 수칙, 소스 순서, 작업 등급(Trivial/Non-Trivial/High-Risk), 도구 사용 제약 조건 등이 정교하게 정리되어 있음.
- **미비점**:
  - 에이전트의 두 구동 엔진(Gemini 3.5 Flash / 3.1 Pro)의 물리적 성능 격차와 이에 따른 **실행 아키텍처 분기 규칙(Routing Logic)**이 누락되어 있음.
  - 대용량 컨텍스트 윈도우가 지원됨에 따라 발생할 수 있는 "무분별한 전체 코드 읽기"를 제어하는 **컨텍스트 이코노미(Context Economy) 가이드라인**의 강제성이 부족함.
  - 고성능 모델들이 더 똑똑해짐에 따라 발생할 수 있는 "임의 리팩토링(Speculative Polish)" 및 "가독성 목적의 주석/메타데이터 임의 제거"에 대한 구체적인 수술적 편집(Surgical Edit) 강제 지침이 부족함.

### [점검 2] `agent-workflow-pipeline.md` (Stage-by-Stage Workflow)
- **현황**: Discovery -> Impact Analysis -> Code Gen & Review로 이어지는 정밀한 도구 사용 절차가 정의되어 있음.
- **미비점**:
  - **이중 속도(Two-Speed) 도구 전략** 부재: 3.5 Flash의 초고속 툴 호출 특성과 3.1 Pro의 무거운 그래프 분석 특성에 따른 단계별 도구 사용 가중치 튜닝이 반영되지 않음.
  - 대용량 빌드/컴파일 로그 처리 시, 3.5 Flash의 요약 기능(`semble_rs digest`) 사용 시점이 구체화되지 않아 텍스트 버퍼 오버플로우 위험이 잔존함.

### [점검 3] JHipster Spring Boot 3 + Vue 3 스택 준수 여부
- **현황**: JPA Expert, JHipster Vue Standards, Pinia State Management 등 각 스택별 Local Skills가 `.agents/skills/`에 우수하게 갖춰져 있음.
- **미비점**:
  - Monorepo 경계를 넘어설 때(Spring Boot DTO 변경이 Vue 3 컴포넌트에 미치는 영향 분석 등) 3.1 Pro 모델의 정밀 역추적(`code-review-graph` & Serena LSP find_referencing_symbols) 절차가 `AGENTS.md` 레벨에서 유기적으로 결합되어 있지 않음.

---

## 3. 핵심 추천 및 개선 사항 (Actionable Recommendations)

### 💡 추천 1: 3.5 Flash와 3.1 Pro의 역할 기반 분기 설계 (Dual-Engine Routing)
- **Trivial/Fast Track 태스크** (오타 수정, 컴포넌트 단순 스타일링, 개별 테스트 보완 등):
  - **Gemini 3.5 Flash**를 주동력으로 사용하고, 초고속 툴 탐색을 체인으로 연결하여 10초 이내에 최종 완료하는 롤플레잉 지침을 명시합니다.
- **Non-trivial/High-Risk 태스크** (보안/OAuth2, Spring JPA 아키텍처, Liquibase 마이그레이션, Pinia 전역 상태, 다면 파일 수정 등):
  - **Gemini 3.1 Pro**를 사용하여 `.agents/skills/superpower/SKILL.md` (BPI 프로세스)를 엄격히 강제하고, 무단 변경을 100% 방지하는 심층 검증(Surgical Plan & Validation) 단계를 거칩니다.

### 💡 추천 2: Context Guard의 강화 (Lost in the Middle 방지)
- 초대형 컨텍스트 윈도우(1M+ tokens) 환경에서도 **"Surgical Reading"** 법칙을 준수해야 합니다.
- 파일 크기가 500라인 이상인 경우 전체 파일을 여는 것 대신, Serena LSP 툴로 기호(Symbol)의 정의와 호출부만 정밀 핀포인트로 읽어 들이도록 강제합니다.
- 컴파일/빌드 에러 로그를 발견할 시, 즉시 `semble_rs digest`를 래핑하여 에러의 본질만 압축 후 프롬프트에 제공함으로써 컨텍스트 오염을 최소화합니다.

### 💡 추천 3: Surgical Edit 및 무단 메타데이터 추가 금지 규칙 고도화
- 고성능 모델일수록 코드 리팩토링이나 포맷 변환 시 의도치 않은 메타데이터 주석(예: `// Added by Antigravity`)을 코드 내에 삽입하거나 필요한 에러 핸들러를 임의로 단순화할 수 있습니다.
- "코드 주석에 한글 사용 규칙(Korean Comment Rule)"과 "소스 코드 내 변경 이력 코멘트 작성 절대 금지(No Pollution Rule)"를 기존보다 더 강력한 프롬프트 가드로 묶어둘 것을 추천합니다.

---

## 4. 향후 가이드라인 반영 액션 플랜

1. **Phase 1: `AGENTS.md` 내에 "2D. Model-Specific Execution Guidelines" 추가** (제안서 기반 개정)
2. **Phase 2: `docs/workflow/agent-workflow-pipeline.md` 내에 이중 속도 도구 사용 흐름 명시**
3. **Phase 3: 변경된 가이드라인 반영 후 `scripts/sync-docs-routing-cache.sh` 실행 및 교차 검증**

본 보고서에 명시된 분석 결과와 추천 사항을 기반으로 가이드라인 개정을 승인해 주시면, 안전하게 반영 작업을 시작하도록 하겠습니다.
