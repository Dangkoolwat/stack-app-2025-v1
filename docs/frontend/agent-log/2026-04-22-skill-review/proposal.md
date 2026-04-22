---
agent: Gemini 3 Flash
created_at: 2026-04-22 (수요일)
language: ko
---

# 방안 1: 프로젝트 최적화 스킬 셋 구성 (권장)
현재 기술 스택에 최적화된 스킬들만 남기고, 부족한 스킬(Oracle, Redis, Testcontainers, JHipster)을 식별하여 추후 설치 목록에 추가합니다.

- **유지(Keep)**: `architecture-patterns`, `spring-boot-*`, `vue`, `graphify`, `playwright`, `ui-ux-pro-max`, `grammar-checker`, `humanizer`, `style-guide`, `scan-code-review-rules`.
- **제거(Remove)**: `shadcn`, `shadcn-ui`, `openai-docs`, `readme-i18n`.
- **추가 예정(To Install)**: `jhipster-expert`, `testcontainers-patterns`, `oracle-sql-standards`, `redisson-cache-patterns`.

## 선택 이유
- 프로젝트의 핵심 기술 스택인 Spring Boot 4와 Vue 3, Bootstrap 5 환경에 집중하기 위함입니다.
- 불필요한 Shadcn/Tailwind 관련 혼선을 방지하여 시니어 아키텍트급 정밀도를 유지합니다.

## 리스크
- 추후 Tailwind나 OpenAI를 도입할 경우 다시 설치해야 하는 번거로움이 있으나, 현재 스택과는 거리가 멉니다.

# 방안 2: 최소 기능 유지 및 전체 스킬 보존
현재 스킬들을 모두 유지하되, `AGENTS.md`에 기술 스택 우선순위를 명시하여 혼선을 방지합니다.

## 선택 이유
- 스킬 삭제로 인한 잠재적 기능 상실을 방지합니다.

## 리스크
- 에이전트가 여전히 프로젝트와 무관한 도구(Shadcn 등)를 추천할 가능성이 높습니다.
