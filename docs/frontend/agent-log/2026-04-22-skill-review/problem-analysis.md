---
agent: Gemini 3 Flash
created_at: 2026-04-22 (수요일)
language: ko
---

# 문제 현상
현재 프로젝트에 설치된 로컬 스킬(Local Skills)들이 프로젝트의 실제 기술 스택(Spring Boot 4, Vue 3, Bootstrap 5, Oracle, Redis 등)과 완벽하게 일치하지 않거나, 사용되지 않는 스킬들이 포함되어 있어 효율적인 작업 수행에 방해가 될 수 있음.

# 재현
- `package.json` 및 `pom.xml` 분석 결과: Bootstrap 5, Vue 3, Spring Boot 4, Oracle, Redis 사용 확인.
- 현재 설치된 스킬 목록 확인: `shadcn`, `shadcn-ui`, `openai-docs` 등 프로젝트 스택과 무관한 스킬들이 포함됨.

# 원인
프로젝트 초기화 또는 이전 작업 과정에서 범용적인 스킬들이 대거 설치되었으나, 실제 프로젝트의 특화된 요구사항(JHipster 기반, 특정 DB/Cache 모듈)을 반영한 스킬은 부족함.

# 영향
- AI 에이전트가 프로젝트 스택에 맞지 않는 제안(예: Bootstrap 프로젝트에서 Tailwind/Shadcn UI 제안)을 할 가능성 있음.
- 불필요한 스킬 로딩으로 인한 컨텍스트 낭비 및 혼선 발생.
