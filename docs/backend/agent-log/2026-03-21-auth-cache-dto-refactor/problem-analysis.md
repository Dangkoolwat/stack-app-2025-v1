# problem-analysis.md

## 문제 현상

Spring Boot 4 + Jackson 3 + Redisson 환경에서 JPA 엔티티를 Redis 에 직접 캐시하면서
직렬화/역직렬화 오류가 반복 발생.
이전 에이전트(C-1 작업, 2026-03-20)가 `User`, `Authority` 엔티티의 Redis 캐시를 전면 제거했고,
그 결과 아래 두 가지 부작용이 발생한 상태였음:

1. 인증 요청마다 DB 조회 발생 → 트래픽 증가 시 DB 부하 위험
2. `CommonCodeService`, `UploadService` 에도 동일한 JPA 엔티티 직접 캐시 문제 잔존

## 재현 방법

- 애플리케이션 기동 후 로그인 API(`POST /api/authenticate`) 호출
- Redis 에 저장된 `User$$HibernateProxy$xxxx` @class 값으로 역직렬화 시도 → 실패
- `CommonCodeService.findAllGroups()` 호출 시 `@Cacheable`이 `CommonCodeGroup` 엔티티 직렬화
  → LazyLoading 세션 소멸 오류

## 추정 원인

JPA 엔티티는 Hibernate 가 런타임에 `$$HibernateProxy$$xxxx` 형태의 프록시 클래스로 감싸는데,
Jackson 3 의 `DefaultTyping` 활성화 상태에서 이 클래스명이 `@class` 필드에 저장됨.
역직렬화 시 해당 클래스가 존재하지 않아 실패.

추가로 `@ManyToMany authorities` 같은 LazyLoading 컬렉션은 Redis 저장 시점에
영속성 컨텍스트가 소멸되어 직렬화 자체가 불가능함.

이전 에이전트들은 "캐시 자체의 문제"와 "캐시 대상 객체의 설계 문제"를 구분하지 못해
캐시 전체를 제거하는 잘못된 결론을 내렸음.

## 영향 범위

| 서비스 | 문제 유형 | 영향 |
|--------|----------|------|
| `DomainUserDetailsService` | 인증 캐시 없음 | 매 요청 DB 쿼리 발생 |
| `CommonCodeService` | `@Cacheable` 엔티티 직접 캐시 | 역직렬화 실패 위험 |
| `UploadService` | `Upload` 엔티티 직접 캐시 | 역직렬화 실패 위험 |
| `Architecture.md` | 잘못된 금지 규칙 표현 | 에이전트 오판 유발 |
| `Engineering_Guideline.md` | 모호한 캐시 금지 목록 | 에이전트 오판 유발 |
| `AGENTS.md` | Cache Safety 규칙 미흡 | 에이전트 오판 유발 |
