---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# 구현 흐름

1. `CacheConfiguration` 에서 Hibernate L2 region 생성과 startup `clear()` 를 제거했다.
2. Redisson client 는 JSON codec 기반 단일 클라이언트로 정리하고 기존 `redissonJsonClient` 별칭을 유지했다.
3. `spring.jpa.properties.hibernate.cache.use_second_level_cache`, `use_query_cache` 를 비활성화했다.
4. 엔티티의 Hibernate `@Cache` 선언을 제거해 DTO 캐시 정책과 충돌하지 않도록 맞췄다.
5. `CommonCodeService` 는 캐시 HIT 시 엔티티 복원 대신 `GroupDto`, `DetailDto` 를 그대로 반환하도록 변경했다.
6. `UploadService` 는 `findById`, `findAllByBoard`, `getAuthorizedPrivateUpload` 를 DTO 반환으로 정리하고 cache failure fallback 을 추가했다.
7. `GlobalSettingsService` 는 cache read/write/evict 예외를 삼키고 DB fallback 하도록 보강했다.
8. `increaseDownloadCount()` 에서 upload cache eviction 을 추가해 stale count 가 남지 않도록 했다.
9. 관련 단위 테스트를 DTO 계약과 fallback/eviction 기준으로 갱신했다.

# 핵심 포인트

- 캐시 대상은 DTO/read-model 이고, 쓰기 로직은 여전히 DB 엔티티를 기준으로 동작한다.
- API 응답의 주요 필드 구조는 유지하되, 캐시 내부 구현은 엔티티 복원에 의존하지 않도록 바꿨다.
- 통합 테스트 환경 문제는 별도 블로커로 분리했다.
