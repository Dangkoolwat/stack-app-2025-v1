# Backend Architecture & Data Structure — stack-app-2025-v1

## 1. 개요 및 지식 정렬 (Knowledge Alignment)
본 문서는 JHipster 프레임워크 기반의 Spring Boot 백엔드 설계 표준을 정의합니다.
모든 AI 코딩 에이전트(AI Agent)는 작업을 시작하기 전 반드시 본 문서와 `Engineering_Guideline.md`를 최우선 컨텍스트로 로드하고, 모든 제안과 코드 생성에 이를 반영해야 합니다.

## 2. 4계층 구조 및 역할 (Layered Architecture)
- Web Layer (Resource): REST 컨트롤러 계층. HTTP 요청 처리 및 DTO-Entity 매핑을 수행합니다.
- *가드레일*: Entity를 직접 노출하지 않으며, 반드시 DTO를 통해 클라이언트와 통신합니다.
- Service Layer: 비즈니스 로직의 심장. 트랜잭션(@Transactional) 관리 및 도메인 로직을 처리합니다.
- Repository Layer: Spring Data JPA를 사용한 데이터 접근 계층입니다. 오라클 전용 쿼리 최적화를 고려합니다.
- Domain Layer (Entity): 데이터베이스 테이블과 매핑되는 핵심 도메인 모델입니다.

## 3. 데이터베이스 환경: Oracle DB
- 개발 환경: Docker 기반 Oracle 컨테이너를 사용하여 로컬 환경의 일관성을 유지합니다.
- 상용 환경: Oracle 상용 에디션(On-prem/Cloud)을 사용합니다.
- 스키마 관리: 모든 DB 변경 사항은 `src/main/resources/config/liquibase/` 경로의 changelog를 통해서만 관리하며, 수동 SQL 조작은 금지됩니다.

## 4. 오라클 및 DB 설계 핵심 원칙 (Database Guardrails)
- P1. 식별자 명명 규칙: 테이블 및 컬럼명은 오라클 호환성을 위해 30자 이내로 작성하며, 가독성을 위해 언더바(`_`)를 사용하는 스네이크 케이스를 따릅니다.
- P2. 시퀀스 전략: 모든 PK는 오라클 `SEQUENCE` 전략을 사용합니다. JHipster 관례에 따라 시퀀스 명명 규칙을 준수합니다.
- P3. 대소문자 주의: 오라클은 기본적으로 대소문자를 구분하지 않으나(Metadata 상 대문자 저장), JPA 매핑 시 예기치 못한 Double Quotes(`"`) 이슈가 발생하지 않도록 주의합니다.
- P4. 데이터 타입 최적화:
  - 문자열은 `VARCHAR2`를 기본으로 사용합니다.
  - 날짜는 시간대 정보를 포함하는 `TIMESTAMP WITH TIME ZONE` 또는 JHipster 표준을 따릅니다.

## 5. 보안 및 에러 표준
- 인증: JWT 기반 Stateless 보안 시스템을 따릅니다.
- 에러 응답: RFC 7807 표준을 준수하는 `ProblemDetail` 형식을 클라이언트에 반환합니다.
