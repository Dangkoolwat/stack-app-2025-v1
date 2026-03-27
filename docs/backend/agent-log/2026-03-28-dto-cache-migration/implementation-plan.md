---
agent: GPT-5.4
created_at: 2026-03-28 (Sat)
language: ko
---

# 단계

1. `CacheConfiguration` 을 DTO 전용 JCache 등록 구조로 단순화
2. Hibernate L2 Redis 비활성화 및 관련 엔티티 `@Cache` 제거
3. 전역 Jackson 설정에서 캐시 우회용 `@JsonIgnore` 무시 제거
4. `CommonCodeService`, `UploadService`, `GlobalSettingsService` 의 캐시 read path 를 DTO/fallback 중심으로 정리
5. 컨트롤러와 테스트를 새 계약에 맞게 수정

# 변경 파일

- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
- `src/main/java/com/daangcool/stack/config/JacksonConfiguration.java`
- `src/main/resources/config/application.yml`
- `src/main/java/com/daangcool/stack/service/common/CommonCodeService.java`
- `src/main/java/com/daangcool/stack/service/board/UploadService.java`
- `src/main/java/com/daangcool/stack/service/GlobalSettingsService.java`
- `src/main/java/com/daangcool/stack/web/rest/common/CommonCodeResource.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadResource.java`
- `src/main/java/com/daangcool/stack/web/rest/UploadAdminResource.java`
- `src/test/java/com/daangcool/stack/config/CacheConfigurationIT.java`
- `src/test/java/com/daangcool/stack/service/common/CommonCodeServiceT.java`
- `src/test/java/com/daangcool/stack/service/board/UploadServiceT.java`
- `src/test/java/com/daangcool/stack/service/GlobalSettingsServiceT.java`

# 테스트 포인트

- 캐시 HIT/MISS
- 캐시 장애 fallback
- mutation 후 eviction
- startup clear 제거
- upload download count 변경 시 stale cache 방지
