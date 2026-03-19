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

### 3.1 DB 사용 제한 정책 (Critical Guardrail)

- 본 시스템은 Oracle DB를 유일한 데이터베이스로 사용한다
- In-memory DB(H2, HSQL 등)는 어떤 환경에서도 사용하지 않는다
- 로컬 개발 환경에서도 Oracle 컨테이너를 기준으로 실행해야 한다

#### 금지 사항
- H2 dependency 추가
- 테스트 편의를 위한 DB 변경
- DB 연결 실패 시 대체 DB 도입

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

## 6. JSON & Serialization Standard (Jackson Policy)

### 6.1 기본 원칙
본 프로젝트는 Spring Boot 4 기반이며, JSON 직렬화 표준은 Jackson 3를 기준으로 한다.

- Jackson 3 (`tools.jackson` 계열)을 단일 표준으로 사용한다
- Jackson 2 (`com.fasterxml.jackson`)는 사용하지 않는다
- 두 계열의 혼용은 금지한다

### 6.2 금지 사항
- Jackson 2 + Jackson 3 혼용
- ObjectMapper 직접 생성 (`new ObjectMapper()`)
- 캐시용과 API용 ObjectMapper 분리

### 6.3 적용 규칙
- ObjectMapper는 반드시 Spring Bean으로 주입받아 사용한다
- 모든 직렬화 계층(API, Cache, OpenAPI)은 동일한 mapper 체계를 사용한다
- transitive dependency까지 포함하여 Jackson 2는 완전히 제거한다

### 6.4 목적
- 직렬화/역직렬화 불일치 방지
- 캐시 데이터 안정성 확보
- Swagger/OpenAPI 일관성 유지

## 7. Cache & Redis Architecture Policy

### 7.1 기본 원칙

본 시스템의 캐시 전략은 다음을 따른다:

- Redis 연결은 중앙 집중형으로 관리한다
- 서비스마다 Redis 연결을 생성하지 않는다
- 캐시는 연결이 아니라 “영역(cache name)”으로 분리한다

### 7.2 Redis 연결 전략

#### 올바른 방식
- 공용 RedissonClient 사용
- 필요 시 목적별 최소 Bean만 허용
- cache namespace 기반 분리

#### 금지
- 서비스마다 RedisClient 생성
- 기능별 Redis 연결 추가
- 인증 캐시를 별도 Redis로 분리

### 7.3 Cache 책임 분리

- 캐시는 서비스 레이어에서 관리한다
- 각 서비스는 자신의 cache key, TTL, eviction 정책을 가진다
- 인프라 레벨에서 비즈니스 로직을 알지 않는다

---

### 7.4 Cache 유형 분리

#### Hibernate L2 Cache
- 엔티티 레벨 캐시
- Binary codec 사용
- ORM 성능 최적화 목적

#### Application Cache (@Cacheable)
- 서비스 응답 캐시
- JSON 직렬화 가능
- 명확한 TTL 필수

---

### 7.5 인증 캐시 정책 (중요)

다음 데이터는 캐시 대상에서 제외한다:

- 사용자 인증 정보
- 로그인 처리 결과
- UserDetails
- 권한 정보
- 계정 상태 (활성화/잠금 등)

#### 이유
- stale 데이터 → 보안 문제
- JWT 구조에서는 캐시 이점이 낮음
- DB 상태와 캐시 상태 불일치 위험

---

### 7.6 TTL 전략

- 설정 / 공통코드 → Long TTL
- 조회 데이터 → 중간 TTL
- 통계 / 카운트 → 짧은 TTL
- 인증 관련 → 캐시 금지

---

### 7.7 직렬화 정책

- 동일 데이터는 동일 codec 사용
- Binary / JSON 혼용 시 영역 분리
- ObjectMapper는 반드시 단일 체계 유지

---

### 7.8 최종 원칙 요약

- Redis 연결은 적게
- Cache는 서비스 단에서 분리
- 인증은 캐시하지 않는다
- 직렬화는 단일 체계 유지
