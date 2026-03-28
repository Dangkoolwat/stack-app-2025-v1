---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# Cache Strategy Report

## 1. 목적

이 문서는 현재 프로젝트의 캐시 전략을 실무 관점에서 이해하고, 향후 일관된 기준으로 캐시를 설계하기 위한 분석 보고서다.

이번 프로젝트는 JHipster 기본 전략을 따라 Hibernate 2nd cache와 query cache를 포함한 엔티티 중심 캐시를 일부 사용해 왔다. 그러나 Spring Boot 4 + Jackson 3 마이그레이션 과정에서 캐시 직렬화, Hibernate proxy, lazy loading, 무효화 책임 경계와 관련된 문제가 드러났고, 이를 계기로 DTO 중심 Redis cache 전략을 재평가하게 되었다.

핵심 질문은 단순히 "Spring Boot 4에서 깨졌는가"가 아니라, "이 시스템에 어떤 캐시 전략이 더 적합한가"이다.

## 2. 핵심 결론

1. Hibernate 2nd cache는 나쁜 기술이 아니다.
2. 다만 현재 프로젝트처럼 REST API 중심, DTO 응답 중심, Redis 기반 애플리케이션 캐시를 이미 운영하는 구조에서는 DTO cache가 더 안정적이고 운영 친화적이다.
3. 이번 문제는 Spring Boot 4가 만든 문제라기보다, 기존 엔티티 캐시 전략의 취약점을 Spring Boot 4 + Jackson 3 환경이 더 분명하게 드러낸 사건에 가깝다.
4. 앞으로의 기본 전략은 DTO/read-model cache로 두고, Hibernate 2nd cache는 예외적 최적화 옵션으로 제한 검토하는 것이 적절하다.

## 3. 캐시 전략의 큰 구분

### 3.1 Hibernate 2nd cache

의미:
- Hibernate가 엔티티와 연관 컬렉션을 세션 밖 캐시에 저장해 재사용하는 ORM 내부 최적화 전략

목표:
- 같은 엔티티를 반복 조회할 때 DB 접근을 줄임

특징:
- 엔티티 그래프, 연관관계, 프록시, lazy loading과 강하게 연결됨
- query cache와 조합할 수 있음
- ORM 동작 이해가 부족하면 stale data와 디버깅 난도가 올라감

### 3.2 DTO cache / Read-model cache

의미:
- 서비스 또는 API가 실제로 반환하는 읽기 결과를 DTO 또는 read model 형태로 Redis 등에 저장하는 애플리케이션 레벨 최적화 전략

목표:
- 비싼 읽기 결과를 재사용하고, API 응답 비용과 DB hit를 줄임

특징:
- 엔티티 대신 최종 응답 경계에 가까운 모델을 캐시
- 직렬화, TTL, invalidation, fallback을 서비스 기준으로 설계 가능
- 프록시와 영속성 컨텍스트 문제를 피하기 쉬움

## 4. 왜 이번 프로젝트에서 DTO cache가 더 맞는가

현재 프로젝트는 다음 성격을 가진다.

- REST API 중심 서비스
- DTO 응답이 많고, 컨트롤러 경계가 비교적 명확함
- Redis를 애플리케이션 캐시 및 운영성 기능에서 함께 사용함
- 공통코드, 설정, 게시글, 업로드, 태그 등 읽기 위주 데이터가 다수 존재함
- Spring Boot 4 + Jackson 3 환경에서 Hibernate proxy 및 직렬화 영향이 민감하게 드러남

이 구조에서는 엔티티 그래프 자체를 캐시하기보다, 최종 읽기 결과를 DTO로 캐시하는 편이 다음 이유로 더 낫다.

- 직렬화 결과가 예측 가능함
- API 계약과 캐시 계약을 맞추기 쉬움
- Redis 장애 시 DB fallback 설계가 단순함
- 캐시 무효화 책임을 서비스 단위로 정의하기 쉬움
- Hibernate 내부 상태 변화에 덜 민감함

## 5. Spring Boot 4 이슈와 실무 이슈의 구분

### 5.1 Spring Boot 4 / Jackson 3가 직접 드러낸 부분

- Jackson 3 마이그레이션 이후 직렬화/역직렬화 계약이 더 민감해짐
- Hibernate proxy 타입이나 lazy relation이 Redis JSON payload와 더 쉽게 충돌함
- 기존에 암묵적으로 허용되던 전역 ObjectMapper 우회 전략이 더 위험해짐

### 5.2 원래부터 존재하던 실무 리스크

- 엔티티 캐시와 query cache의 무효화 복잡도
- 연관관계가 복잡한 엔티티 캐시의 stale data 위험
- 운영 중 캐시 장애가 ORM 내부 동작과 섞여 문제를 키울 가능성
- 애플리케이션 캐시와 Hibernate 캐시의 역할 혼선

정리하면, Spring Boot 4는 문제를 만든 유일 원인이라기보다 기존 전략의 취약점을 강하게 노출한 계기다.

## 6. 실무에서 DTO cache는 많이 쓰는가

많이 쓴다. 특히 다음과 같은 시스템에서는 매우 흔하다.

- REST API 서비스
- MSA 또는 외부 API 연동이 많은 서비스
- 응답 조합 비용이 큰 서비스
- Redis 기반 캐시를 표준적으로 사용하는 조직
- JPA/Hibernate를 쓰더라도 API 응답은 DTO 중심인 서비스

실무에서 자주 보는 DTO cache 패턴:

1. 설정값 cache
2. 공통코드 cache
3. 단건 상세 조회 DTO cache
4. 목록/페이지 결과 cache
5. 검색 결과 cache
6. 대시보드/통계 snapshot cache
7. 외부 API 응답 cache
8. 사용자별 요약 정보 cache
9. 권한/인가 요약 read-model cache
10. 워크플로우 임시 상태 cache

즉 DTO cache는 예외적 우회책이 아니라, 운영 친화적인 표준 패턴 중 하나다.

## 7. Hibernate 2nd cache가 잘 맞는 경우

다음 조건이 동시에 어느 정도 맞아야 한다.

- 읽기 비율이 매우 높음
- 엔티티 구조가 단순함
- 변경 빈도가 낮음
- 무효화 범위를 예측하기 쉬움
- lazy relation과 proxy 영향이 제한적임
- 팀이 Hibernate 캐시 동작을 충분히 이해하고 있음
- 실제 측정상 DB hit 감소 효과가 분명함

예시:
- 거의 변경되지 않는 기준 엔티티
- 연관관계가 단순한 코드성 엔티티
- 특정 테이블 기반의 반복적인 엔티티 조회

## 8. DTO cache가 잘 맞는 경우

- API 또는 서비스 최종 응답이 이미 DTO 중심일 때
- 여러 테이블/외부 API/설정을 조합한 결과일 때
- 엔티티 그래프보다 읽기 결과를 재사용하는 편이 더 효율적일 때
- 무효화 주체를 서비스 차원에서 명시할 수 있을 때
- 직렬화 안정성이 중요할 때
- Redis 장애 시 fallback 경로를 단순하게 유지하고 싶을 때

예시:
- 게시글 상세 응답 DTO
- 게시판 페이지 결과 DTO 목록
- 공지사항 목록 DTO
- 업로드 메타데이터 DTO
- 설정 DTO
- 공통코드 DTO

## 9. DTO cache를 피하거나 조심해야 하는 경우

- 값이 매우 자주 바뀌어 stale data 허용이 거의 없을 때
- 캐시 키 조합이 지나치게 많아 cardinality가 폭증할 때
- 계산 비용이 작아서 캐시 이득이 거의 없을 때
- 민감 정보가 직접 포함될 위험이 있을 때
- 사용자별/권한별 분기가 너무 많아 메모리 효율이 나쁠 때
- 무효화 지점이 너무 많아 운영 복잡도가 더 커질 때

예시:
- 실시간 재고
- 매우 자주 변하는 카운터
- 즉시 정합성이 절대적인 금융성 수치
- 캐시 key 차원이 과도한 자유 검색 결과

## 10. 현재 프로젝트에 대한 적용 예제

### 10.1 설정값 cache

대상:
- `GlobalSettingsService`

이유:
- 읽기 비율 높음
- 변경 빈도 낮음
- DTO 형태가 단순함

권장:
- 긴 TTL
- 설정 변경 시 명시적 evict
- Redis 실패 시 DB fallback

### 10.2 공통코드 cache

대상:
- `CommonCodeService`

이유:
- 대표적인 코드성 데이터
- 목록/상세 조회 반복 빈도 높음
- DTO로 표현하기 적합함

권장:
- 그룹/상세별 DTO cache
- create/update/delete/soft delete 시 관련 cache 일괄 invalidation

### 10.3 게시글 상세 및 목록 cache

대상:
- `BOARD_BY_ID`
- `BOARD_PAGE`
- `BOARD_SEARCH`
- `BOARD_NOTICES`

이유:
- 읽기 부하가 큰 영역
- 최종 응답은 사실상 read model에 가까움

권장:
- 상세는 단건 DTO cache
- 목록과 검색은 TTL을 더 짧게 두고 invalidation 범위를 명확히 관리

주의:
- 댓글 수, 태그, 첨부 수 등 집계 필드가 응답에 포함되면 연쇄 무효화 범위를 설계해야 함

### 10.4 업로드 메타데이터 cache

대상:
- `UPLOAD_BY_ID`
- `UPLOAD_BY_BOARD`

이유:
- 파일 자체가 아니라 메타데이터 조회가 반복됨

권장:
- `UploadDTO` 기반 cache
- 파일 삭제, 공개 여부 변경, 연결 게시글 변경 시 evict

주의:
- `downloadCount`처럼 자주 변하는 값은 stale data 리스크가 있으므로 짧은 TTL 또는 즉시 evict 필요

### 10.5 태그 cache

대상:
- `TAG_ALL`
- `TAG_PREFIX`
- `TAG_POPULAR`

이유:
- 조회 빈도가 높고 대체로 코드성/집계성 성격이 강함

권장:
- prefix 검색과 인기 태그는 read model cache로 유지
- 태그 변경 시 관련 범위를 묶어서 invalidation

## 11. 적용 예제 코드 패턴

### 11.1 권장 패턴

```java
@Transactional(readOnly = true)
public Optional<UploadDTO> findById(Long id) {
    Cache cache = cacheManager.getCache(UPLOAD_BY_ID);
    if (cache != null) {
        try {
            UploadDTO cached = cache.get(id, UploadDTO.class);
            if (cached != null) {
                return Optional.of(cached);
            }
        } catch (RuntimeException ex) {
            log.warn("Upload cache read failed for id {}", id, ex);
        }
    }

    Optional<UploadDTO> loaded = uploadRepository.findById(id).map(uploadMapper::toDto);
    loaded.ifPresent(dto -> putQuietly(cache, id, dto));
    return loaded;
}
```

장점:
- cache hit/miss/fallback 흐름이 분명함
- 엔티티를 직접 cache 하지 않음
- 예외 발생 시 서비스 전체가 죽지 않음

### 11.2 피해야 할 패턴

```java
@Cacheable("uploads")
public Upload findById(Long id) {
    return uploadRepository.findById(id).orElseThrow();
}
```

문제:
- 엔티티 자체를 cache payload로 사용
- proxy/lazy relation/직렬화 영향에 취약
- API 응답 경계와 캐시 경계가 섞임

## 12. 캐시 선택 체크리스트

새 cache를 추가할 때는 아래를 먼저 답해야 한다.

1. 이 데이터는 엔티티인가, 읽기 결과인가?
2. 최종 응답과 거의 같은 DTO로 표현 가능한가?
3. 읽기 빈도가 충분히 높은가?
4. stale data 허용 범위는 어느 정도인가?
5. 무효화 주체는 누구인가?
6. TTL은 얼마가 적절한가?
7. Redis 장애 시 fallback은 무엇인가?
8. 민감 정보가 섞이지 않는가?
9. 테스트로 hit/miss/evict/fallback을 검증할 수 있는가?
10. 실제 측정상 이득이 있는가?

## 13. 권장 운영 정책

현재 프로젝트의 기본 정책은 다음과 같이 가져가는 것이 좋다.

- 기본값: DTO/read-model cache
- 예외적 최적화: 제한적 Hibernate 2nd cache
- query cache: 더 보수적으로 검토
- startup 시 cache clear 금지
- 전역 ObjectMapper 의미를 cache 때문에 바꾸지 않음
- cache 추가 시 DTO 계약, TTL, invalidation owner, fallback, 테스트를 함께 정의

## 14. 향후 재도입 검토 기준

향후 Jackson 3 관련 문제가 충분히 해소되더라도, Hibernate 2nd cache 재도입은 다음 조건을 만족할 때만 검토하는 것이 바람직하다.

- 대상 엔티티가 단순함
- 읽기 편향이 강함
- stale risk가 낮음
- 무효화 범위가 제한적임
- 실측상 DTO cache보다 이득이 있음
- 운영/테스트 복잡도를 감수할 가치가 있음

즉 "기술적으로 가능하다"와 "기본 전략으로 채택할 만하다"는 별개다.

## 15. 최종 정리

이번 프로젝트에서 얻은 가장 중요한 교훈은 다음과 같다.

- 캐시는 단순 성능 기능이 아니라 아키텍처 선택이다.
- Spring Boot 4는 기존 문제를 드러낸 계기였지만, 본질은 시스템에 맞는 캐시 전략의 선택 문제다.
- 현재 프로젝트는 ORM 내부 최적화보다 API/read-model 최적화에 더 가까운 구조다.
- 따라서 DTO cache를 기본 전략으로 두는 것이 더 실무적이고 안전하다.

향후 새로운 캐시를 추가할 때도 "엔티티를 cache 할 수 있는가"보다 "어떤 읽기 결과를 어떤 책임 경계에서 재사용할 것인가"를 먼저 묻는 방향이 적절하다.
