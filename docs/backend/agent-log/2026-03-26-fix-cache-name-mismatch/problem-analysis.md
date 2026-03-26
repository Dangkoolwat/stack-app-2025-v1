---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

# 문제 분석 (Problem Analysis)

## 문제 현상

공통 코드 상세 생성 API(POST /api/common-code-details)를 호출할 때 서버에서 `java.lang.NullPointerException`이 발생합니다.

## 재현 경로

1.  `https://localhost:9000/common-code-detail/new?groupCode=BOARD_TYPE` 와 같이 새로운 상세 코드를 생성하려고 시도합니다.
2.  `CommonCodeService.createDetail()`이 실행됩니다.
3.  `clearDetailCaches()` 메서드에서 `cacheManager.getCache(COMMON_DETAIL_CACHE)` 호출 결과가 `null`인 상태로 `Objects.requireNonNull()`이 호출되어 NPE가 발생합니다.

## 원인 분석

`CommonCodeService` 및 `GlobalSettingsService` 등 일부 서비스 클래스에서 프로젝트 표준 캐시 이름 상수인 `CacheNames`를 사용하지 않고 개별적으로 문자열 상수를 정의하여 사용하고 있으며, 이 이름이 `CacheConfiguration`에 등록된 실제 캐시 이름과 일치하지 않습니다 (CamelCase vs SnakeCase).

*   **CommonCodeService.java**:
    *   `COMMON_DETAIL_CACHE = "commonDetails"` 사용
*   **CacheNames.java**:
    *   `COMMON_DETAILS = "common_details"` 로 정의됨
*   **CacheConfiguration.java**:
    *   `"common_details"` 이름을 사용하여 캐시를 생성함

또한, `GlobalSettingsService`에서도 다음과 같은 불일치가 발생합니다:

*   서비스: `"SETTING_CACHE"`
*   상수 및 설정: `"settings"`

## 영향도

*   공통 코드 상세 정보 생성/수정/삭제 시 시스템 오류 발생 (NPE)
*   일부 설정 정보 조회 시 캐시가 동작하지 않아 불필요한 DB 조회가 발생하거나 설정 변경이 즉각 반영되지 않을 위험이 있음
