---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# Correctness

- [x] 캐시 설정을 DTO 전용 애플리케이션 캐시 기준으로 재정의
- [x] `CommonCodeService`, `UploadService`, `GlobalSettingsService` 읽기 경계 점검
- [x] 캐시 장애 시 DB/source-of-truth fallback 보강
- [x] upload download count stale cache 방지 반영

# Safety

- [x] 전역 `@JsonIgnore` 무시 설정 제거
- [x] Hibernate L2 Redis 의존 비활성화
- [x] 앱 시작 시 기존 캐시 clear 제거
- [x] 쓰기 경로는 DB 기반 엔티티 조회 유지

# Test

- [x] `./mvnw -q -DskipTests compile`
- [x] `./mvnw -q -Dtest=CacheConfigurationIT,GlobalSettingsServiceT,CommonCodeServiceT,UploadServiceT,UserAuthCacheServiceTest test`
- [ ] `./mvnw -q -Dtest=GlobalSettingsServiceIT test`

미통과 사유
- Testcontainers 기반 통합 테스트에서 `LiquibaseTestConfiguration` 초기화 중 `Hikari - dataSource or dataSourceClassName or jdbcUrl is required` 발생
- 현재 변경분과 직접 무관한 테스트 데이터소스 초기화 블로커로 판단됨
