# 🚀 Serena Integration & Operation Guide (stack-app-2025-v1)

본 가이드는 **Serena(LSP 기반 Semantic Agent)**를 본 프로젝트에 도입하고, AI 에이전트가 코드의 맥락을 완벽하게 이해하도록 설정하는 표준 절차를 정의합니다.

---

## 1. 초기 도입 단계 (Setup Phase)

새로운 프로젝트 환경에서 Serena를 활성화할 때 에이전트에게 다음 프롬프트를 전달하세요.

### 📥 도입 프롬프트
> "이 프로젝트에 Serena MCP를 도입하려고 해. 먼저 프로젝트를 활성화(`activate_project`)하고, 온보딩(`onboarding`) 프로세스를 실행해서 현재 프로젝트의 기술 스택과 아키텍처 핵심 정보를 `memories`에 저장해줘."

### ✅ 체크리스트
- [x] `activate_project` 실행 및 경로 확인
- [x] `onboarding` 도구를 통한 프로젝트 성격 정의
- [x] `memories/` 데이터 저장 확인

---

## 2. 공유 및 협업 설정 (Collaboration)

팀원들과 Serena의 지식을 공유하기 위해 Git 관리가 필요합니다.

### 📦 Git 포함 대상
- `.serena/` 하위의 설정 파일 (필요 시)
- `memories/` 프로젝트 지식 베이스 (Markdown)

### 🚫 Git 제외 대상 (`.gitignore` 반영)
- `/cache`: 로컬 인덱싱 데이터
- `/logs`: 실행 로그

---

## 3. 강력한 성능을 위한 프롬프트 전략 (Advanced Prompting)

작업 시 에이전트가 Serena의 기능을 200% 활용하게 만드는 지시어 예시입니다.

### 🔍 정밀 분석 요청
> "단순히 파일 전체를 읽지 말고, Serena의 `find_symbol`과 `find_referencing_symbols`를 사용해서 [함수/클래스명]의 구현부와 실제 호출되는 모든 위치를 분석한 뒤 영향 범위를 보고해줘."

### 🛠 안전한 리팩토링 요청
> "Serena의 `rename_symbol` 기능을 사용해서 [기존이름]을 [새이름]으로 변경해줘. 이때 단순 텍스트 치환이 아닌 LSP 기반으로 타입 안전성을 보장하며 모든 참조를 업데이트해야 해."

### 🧠 지식 업데이트 요청 (작업 완료 후)
> "이번에 구현한 [기능명]의 핵심 로직과 설계 결정을 Serena `write_memory` 도구를 사용해서 기록해줘. 나중에 다른 에이전트가 이 코드를 수정할 때 참고할 수 있도록."

---

## 4. 에이전트 운영 원칙 (Operating Rules)

본 프로젝트의 `AGENTS.md`와 연동하여 다음 규칙을 적용합니다.

1. **Precision First**: 오타 수정이나 단순 텍스트 변경 외의 모든 **Non-trivial** 작업은 Serena의 심볼 분석을 우선 수행한다.
2. **Memory-Driven**: 새로운 아키텍처 결정이나 복잡한 비즈니스 로직 수정 시 반드시 `memories`에 기록을 남긴다.
3. **Zero Assumption**: 코드를 읽기 전 Serena의 `get_symbols_overview`를 통해 파일 구조를 먼저 파악한다.

---

## 5. 트러블슈팅

- **"No active project" 에러 발생 시**: 에이전트에게 `list_repos`로 등록 여부를 확인하게 한 뒤, `activate_project`를 다시 명령하세요.
- **분석 속도가 느릴 경우**: `get_symbols_overview`의 `depth`를 조정하거나 특정 디렉토리로 범위를 좁히도록 지시하세요.

---

## 6. 토큰 절약 전략 (Token-Saving Protocol)

효율적인 컨텍스트 관리를 위해 다음 우선순위에 따라 도구를 사용합니다:

1. **1순위: `code-review-graph` (구조 분석)**: 전체 코드 구조 파악 및 영향 범위(Blast-radius) 체크.
2.  **2순위: `Serena` (LSP 정밀 작업)**: 실시간 심볼 분석, 호출부 추적 및 정밀 편집.
3.  **3순위: `Grep/Read` (텍스트 분석)**: 비코드 파일(YAML, JSON 등) 검색 및 단순 텍스트 매칭.
4.  **4순위: `git` (이력 분석)**: 변경 사유 및 히스토리 파악.

