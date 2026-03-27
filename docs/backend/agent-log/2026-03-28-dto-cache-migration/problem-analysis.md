---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# 문제 현상

Spring Boot 4 + Jackson 3 환경에서 Redis/Hibernate 2차 캐시에 JPA 엔티티를 직접 올리는 구조가 직렬화, 역직렬화, LazyLoading, 프록시 타입 안정성 측면에서 반복적으로 흔들리고 있었다.

# 재현/근거

- 인증 캐시는 이미 DTO 전용 캐시로 우회한 이력이 있음
- `CacheConfiguration` 에서 Hibernate L2 region 과 애플리케이션 캐시가 혼합 관리되고 있었음
- `CommonCodeService`, `UploadService` 는 캐시 HIT 시 DTO를 다시 엔티티처럼 복원하는 과도기 패턴을 사용하고 있었음
- 전역 Jackson 설정이 캐시 문제를 해결하기 위해 `@JsonIgnore` 의미까지 바꾸고 있었음

# 원인

- 엔티티 캐시와 API 직렬화 규칙이 결합되어 있었음
- 캐시 페이로드가 구체 DTO 대신 프록시/연관관계가 포함된 객체 그래프에 의존했음
- 캐시 장애 시 fallback 보장이 서비스마다 일관되지 않았음

# 영향

- 캐시 포맷 변경 및 마이그레이션 시 운영 리스크 증가
- 읽기 캐시가 서비스 경계를 흐려 향후 회귀 가능성 증가
- 테스트는 일부 단위 검증만 있었고 캐시 fallback, 무효화, stale data 방지 검증이 부족했음
