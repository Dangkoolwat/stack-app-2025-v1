# Problem Analysis: Antigravity Model Update (Gemini 3.5 Flash & 3.1 Pro)

## 1. 개요 및 배경
Antigravity 에이전트의 구동 엔진이 **Gemini 3.5 Flash** 및 **Gemini 3.1 Pro**로 변경됨에 따라, 에이전트의 추론 속도, 컨텍스트 윈도우 처리 방식, 그리고 복잡한 아키텍처 분석 능력에 혁신적인 변화가 발생하였습니다.
기존의 `AGENTS.md` 및 `agent-workflow-pipeline.md`는 레거시 모델(예: Gemini 1.5 계열)의 자원 제약 및 도구 사용 성능 한계를 기준으로 작성되어 있어, 새로운 고성능 모델의 잠재력을 완전히 끌어내지 못하거나, 오히려 대규모 컨텍스트로 인한 주의 분산(Attention Distraction) 및 토큰 낭비(Token Waste) 등의 비효율을 초래할 위험이 있습니다.

이에 따라 현재 가이드라인을 면밀히 점검하고, 모델별 고유한 물리적/논리적 특성을 조화롭게 결합하기 위한 미비점 분석을 수행합니다.

---

## 2. 모델별 특성 정의 및 역할 매핑

### A. Gemini 3.5 Flash (초고속 탐색 및 반복 검증 엔진)
- **핵심 강점**: 
  - 극히 낮은 레이턴시(Ultra-low Latency)와 빠른 응답성
  - 고속 도구 호출(Function Calling) 및 파이프라인 탐색 최적화
  - 대량의 로그 분석(`semble_rs digest`) 및 Trivial 변경 작업의 빠른 처리
- **한계점**:
  - 초거대 아키텍처의 다중 종속성 분석이나 고위험(High-Risk) 설계 결정 시, 복잡한 다단계 추론(Complex Multi-step Reasoning)의 정밀성이 Pro 모델 대비 상대적으로 낮을 수 있음.
- **최적 역할**: **Fast Track 작업, 초기 탐색(Stage 1. Discovery), 빌드/테스트 로그 디제스트, Trivial 수준의 코드 수정 및 검증.**

### B. Gemini 3.1 Pro (복잡한 논리 설계 및 고위험 제어 타워)
- **핵심 강점**:
  - 뛰어난 다단계 논리적 추론 및 알고리즘 최적화 능력
  - Monorepo Boundaries를 넘나드는 복잡한 종속성(blast radius) 분석 및 아키텍처 설계
  - `implementation_plan.md` 및 구조적 아키텍처 제안의 완벽한 수립
- **한계점**:
  - Flash 모델 대비 레이턴시와 비용(토큰 소비 효율) 면에서 다소 무거움.
- **최적 역할**: **Standard Planning 작업, 아키텍처 설계, 보안/인증 등 High-Risk 영역 제어, 복잡한 `code-review-graph` 분석, 다면적 회귀 테스트 설계.**

---

## 3. 현 가이드라인 (`AGENTS.md`) 점검 및 미비점 식별

### 미비점 1: 모델 특성에 따른 의사결정 경로(Execution Path) 분기 부재
- **현상**: `AGENTS.md` Section 3(Task Levels)에서는 작업의 위험도(Trivial, Non-trivial, High-risk)만으로 절차를 나눌 뿐, 어떤 모델이 어떠한 작업 스타일에 적합한지 가이드라인이 명시되어 있지 않습니다.
- **리스크**: Gemini 3.5 Flash가 High-Risk 작업을 수행할 때 필요한 심층 추론(Standard Planning)이 생략되거나, Gemini 3.1 Pro가 단순 오타 수정(Trivial)에 과도한 프롬프트 컨텍스트와 탐색 비용을 낭비하는 비효율이 발생합니다.

### 미비점 2: 거대 컨텍스트 윈도우 지원에 따른 'Context Economy' 가이드 부족
- **현상**: 새 모델들은 최대 1M~2M 이상의 초대형 컨텍스트 윈도우를 지원하므로, 에이전트가 "한 번에 다 읽어도 된다"고 오판하여 대용량 파일이나 전체 디렉토리를 무분별하게 읽어 들이는 경향이 생길 수 있습니다.
- **리스크**: 불필요한 토큰 낭비는 물론, 컨텍스트 내의 노이즈로 인해 Surgical Edit의 정확도가 떨어지고, 엉뚱한 코드를 수정하는 부작용(Lost in the Middle 현상 및 Hallucination)이 발생합니다.

### 미비점 3: 도구 활용 파이프라인의 이중 속도(Two-Speed) 전략 미비
- **현상**: `agent-workflow-pipeline.md`는 도구 사용의 강도 조절에 대한 지침이 고정되어 있습니다.
- **리스크**: 3.5 Flash는 여러 번의 가벼운 도구 호출(예: `search --outline` -> `Serena find`)을 초고속으로 수행하는 것이 효율적이며, 3.1 Pro는 `code-review-graph`와 같은 무겁고 정밀한 분석 도구를 한 번에 정확하게 사용하는 것이 좋습니다. 이러한 특성을 반영한 이중 속도 도구 사용 전략이 누락되어 있습니다.

### 미비점 4: 한국어 명사형 종결어조 규칙의 프롬프트 제어 취약성
- **현상**: `AGENTS.md` Section 17에서는 단순 명사형 종결 어조를 요구하지만, 모델이 고도화될수록 친절하고 장황한 서술식 답변을 출력하려는 성향이 강해집니다.
- **리스크**: 보고서 형식이 장황해져 사용자 가독성이 저하되며, Antigravity 에이전트 특유의 극도로 정밀하고 군더더기 없는 커뮤니케이션 스타일이 훼손됩니다.
