# AGENTS.md

## Purpose
이 문서는 이 저장소에서 협업하는 AI 코딩 에이전트를 위한 프로젝트 수준의 가이드입니다. 여러 에이전트가 일관된 규칙, 추적 가능한 결정, 연속적인 작업 이력을 유지하며 협업하는 것을 목표로 합니다.

## Scope
본 지침은 `stack-app-2025-v1` 저장소에만 적용되며, 글로벌 에이전트 규칙을 확장합니다. 모든 비자명한 작업은 아래에 기술된 규칙에 따라 기록되어야 합니다.

## Project Overview
이 프로젝트는 Spring Boot 4.x 백엔드와 Vue 3 프론트엔드(Vite)가 통합된 Full-Stack 프로젝트입니다.

- Backend Stack: Java 21+, Maven, Spring Data JPA, Liquibase, Redis (Redisson), Spring Security + JWT, RFC7807, Swagger/OpenAPI.
- Frontend Stack: Vue 3, Vite, TypeScript, Pinia, Vue Router, Vitest, Bootstrap-Vue-Next / Bootswatch.

---

## Standard Project Documentation Structure
모든 문서는 docs/ 디렉토리 하위에 위치하며, 영역별로 엄격히 구분합니다. 프로젝트 루트 디렉토리에 새로운 폴더나 파일을 생성하는 것은 엄격히 금지됩니다.
```
docs/
├── backend/                # 백엔드(Spring Boot 4) 관련 문서
│   ├── architecture/       # 백엔드 설계 및 아키텍처 가이드
│   ├── agent-log/          # 백엔드 작업 로그 및 계획서 폴더
│   └── decisions/          # 백엔드 ADR (Architecture Decision Records)
│
├── frontend/               # 프론트엔드(Vue 3) 관련 문서
│   ├── architecture/       # Vue 3 설계 원칙 및 테마 가이드
│   ├── agent-log/          # 프론트엔드 작업 로그 및 계획서 폴더
│   └── decisions/          # 프론트엔드 ADR
```
---

## Agent Work Log & Output Policy
모든 비자명한 작업은 에이전트 로그 정책에 따라 기록되어야 합니다.

### 1. 작업 산출물 위치 강제 (Mandatory Directory)
에이전트가 생성하는 모든 implementation_plan.md, task.md, walkthrough.md 파일은 절대 루트에 두지 않으며, 아래 경로의 작업명 폴더(YYYY-MM-DD-task-name) 내부에서만 관리합니다.
- Backend 작업 시: docs/backend/agent-log/YYYY-MM-DD-task-name/
- Frontend 작업 시: docs/frontend/agent-log/YYYY-MM-DD-task-name/

### 2. 로그 파일 명명 규칙
최종 작업 로그는 YYYY-MM-DD-task-name.md 형식을 따르며, 작업 완료 후 해당 영역의 agent-log/ 폴더에 생성합니다.

---

## Architecture Rules

### Backend Layering
Controller -> Service -> Domain -> Repository 패턴을 따릅니다. 컨트롤러는 얇게 유지하고 비즈니스 로직은 서비스 레이어에 둡니다.

### Frontend Layering (Structure.md 참조)
core(로직) -> themes(표현) -> views(흐름) 계층을 엄격히 분리합니다. 테마 간 독립성을 유지하며 Base 컴포넌트 사용을 권장합니다.

---

## Security & API Rules
- 백엔드 API는 인증, 권한 부여, 유효성 검사 및 에러 계약의 단일 진실 공급원(Source of Truth)입니다.
- 모든 에러 응답은 RFC 7807 형식을 유지해야 합니다.
- 민감한 정보(비밀번호, 토큰 등)는 절대 로그에 남기지 않습니다.

---

## Verification Strategy
모든 변경 사항은 아래 순서로 검증되어야 합니다.
1. 유닛 테스트 (JUnit 5 / Vitest)
2. 통합 테스트 및 API 테스트 (MockMvc)
3. 빌드 검증 (./mvnw clean package 또는 npm run build)

---

## Final Response Structure

**모든 대화 및 최종 응답(Final Response)은 반드시 한국어(Korean)로 작성한다.**

작업 완료 후 응답은 다음을 포함해야 합니다:
- 변경 요약 및 이유
- 수정된 파일 목록
- 수행된 검증 내용
- 남은 리스크 또는 가정 사항
- 에이전트 로그 및 Walkthrough 생성 확인

## Additional Architecture Enforcement

모든 에이전트는 Backend Architecture.md의 다음 정책을 반드시 따른다:

- Jackson 단일화 정책
- Redis 중앙 집중 연결 정책
- 인증 캐시 금지 정책

## Local Execution & Environment Rules (MANDATORY)

모든 에이전트는 로컬 실행 시 다음 규칙을 반드시 따른다:

### 1. .env 기반 실행 강제
- 애플리케이션 로컬 실행은 반드시 `.env` 설정을 기준으로 한다
- DB, Redis, Mail 등 모든 인프라 설정은 `.env`를 Source of Truth로 간주한다
- 설정 오류 발생 시 `.env`를 수정해야 하며, 대체 기술을 도입하지 않는다

### 2. Database 정책 (절대 중요)
- 본 프로젝트의 DB는 Oracle을 기준으로 한다
- H2, HSQL, SQLite 등 In-Memory DB 사용을 금지한다
- 테스트 또는 로컬 실행을 이유로 DB를 변경하지 않는다

### 3. 금지 사항 (Critical Ban)
다음 행위는 절대 금지한다:

- H2 dependency 추가
- application.yml / properties에 H2 설정 추가
- "임시 테스트용" DB로 H2 도입
- DB 연결 실패를 이유로 DB 종류 변경

### 4. 장애 대응 원칙
DB 연결 실패 시:

- 올바른 대응:
  - `.env` 설정 확인
  - Oracle 컨테이너 상태 확인
  - JDBC URL 검증

- 잘못된 대응:
  - H2로 교체
  - Embedded DB 추가
  - DB 설정 우회

### 5. Redis 정책
- Redis는 `.env`에 정의된 설정을 사용한다
- Redis 연결 실패 시 새로운 Redis를 추가하지 않는다