---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

# 구현 계획 (Implementation Plan)

## 개요
`CommonCodeService`와 `GlobalSettingsService`에서 발생하는 캐시 이름 불일치 문제를 해결하기 위해, 모든 캐시 참조를 `CacheNames` 상수를 사용하도록 리팩토링합니다.

## 변경 단계

### 1단계: CommonCodeService 리팩토링
- [CommonCodeService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/common/CommonCodeService.java) 수정
- 내부 캐시 이름 상수 제거 및 `CacheNames` static import 추가
- `cacheManager.getCache()` 호출부 업데이트

### 2단계: GlobalSettingsService 리팩토링
- [GlobalSettingsService.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/GlobalSettingsService.java) 수정
- `SETTING_CACHE` 상수 제거 및 `CacheNames.SETTINGS` 사용

### 3단계: 검증 (Verification)
- 단위 테스트 실행: `CommonCodeServiceT`
- 통합 테스트 실행: `GlobalSettingsServiceIT`
- (수동) 로그를 통한 캐시 클리어 확인

## 변경 파일 목록
- `/Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/common/CommonCodeService.java`
- `/Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/service/GlobalSettingsService.java`
