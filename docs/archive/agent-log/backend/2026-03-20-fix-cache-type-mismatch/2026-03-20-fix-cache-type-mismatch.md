# 작업 로그: 캐시 타입 불일치(SettingsDTO -> Map) 에러 수정 (2026-03-20)

## 1. 개요
`GlobalSettingsService.getSettings()` 호출 시 Redis 캐시가 `SettingsDTO` 대신 `LinkedHashMap`을 반환하여 `IllegalStateException`이 발생하는 문제를 해결했습니다.

## 2. 분석 결과 (Root Cause)
- Jackson 3 전환의 영향: Jackson 3로 업그레이드되면서 `ObjectMapper` 설정 중 `DefaultTyping`이 누락되거나 방식이 변경되어, Redis JSON 저장 시 타입 정보(`@class`)가 포함되지 않게 되었습니다.
- 범위: `SettingsDTO`뿐만 아니라 `BoardDTO`, `TagDTO`, `Upload` 엔티티 등을 사용하는 모든 애플리케이션 레벨의 JSON 캐시(`redissonJsonClient`)에서 동일한 타입 복구 실패 문제가 발생할 수 있음을 확인했습니다.

## 3. 수정 사항

### [Backend] [Cache]
- [CacheConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/CacheConfiguration.java):
    - `objectMapper.rebuild()`를 사용하여 기존 설정(HibernateModule, Introspector 등)을 보존하면서 `DefaultTyping`만 추가 활성화.
    - 이를 통해 `LazyInitializationException` 방지 설정과 프로젝트의 캐시 정책이 모두 유지되도록 수정.
    - [추가] 코드 내부에 수정 이유와 Redis 캐시 초기화 주의사항(flushall)을 상세 주석으로 명시.

### [Frontend] [UI/I18n]
- [I18n]: 영어(`en/settings.json`) 파일에서 누락된 서버 글로벌 설정 키들을 추가하고, 어색했던 메뉴 이름을 "Server Global Settings"로 수정했습니다.
- [Component]: `settings.component.ts`에서 설정 저장 후 이전 페이지로 이동하던 로직(`router.go(-1)`)을 제거하고, 현재 페이지에서 데이터를 재조회하여 상태를 유지하도록 변경했습니다.
- [Menu]: `entities-menu.vue`에서 관리자 메뉴 리스트의 가독성을 위해 메뉴 키를 최적화했습니다.




## 4. 검증 결과

### 자동 테스트
- `RedisCodecTest.java`를 생성하여 직렬화/역직렬화 동작을 직접 검증했습니다.
- 수정 전: JSON에 타입 정보가 없고, 역직렬화 결과가 `LinkedHashMap`임이 확인됨.
- 수정 후: JSON에 `"@class":"com.daangcool.stack.service.dto.SettingsDTO"` 정보가 포함되고, 역직렬화 시 `SettingsDTO` 타입으로 정확히 복원됨을 확인했습니다.

### 빌드 확인
- `./mvnw test -Dtest=RedisCodecTest` 성공 확인.

## 5. 향후 조치 사항 (중요)
- 캐시 초기화: 이전에 타입 정보 없이 저장된 "오염된" 데이터가 Redis에 남아있을 수 있습니다. 애플리케이션 재시작 전 또는 직후에 `redis-cli flushall` 등을 통해 캐시를 완전히 비워줄 것을 권장합니다.

## 6. 생성 문서
- [implementation_plan.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/backend/agent-log/2026-03-20-fix-cache-type-mismatch/implementation_plan.md)
- [task.md](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/docs/backend/agent-log/2026-03-20-fix-cache-type-mismatch/task.md)
- [walkthrough.md](file:///Users/sanghyoukjin/.gemini/antigravity/brain/e8e9846d-54ae-4a15-882e-be006b90bd6f/walkthrough.md) (브레인 디렉토리 내 생성됨)
