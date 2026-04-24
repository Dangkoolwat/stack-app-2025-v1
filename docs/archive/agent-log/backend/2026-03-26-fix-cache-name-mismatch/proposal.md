---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

# 해결 방안 제안 (Proposal)

## 권장 해결 방안 (Primary Direction)
프로젝트 표준인 `CacheNames.java`에 정의된 상수를 모든 서비스에서 동일하게 사용하도록 수정합니다.

1.  **CommonCodeService.java 수정**:
    - 내부에서 정의한 `COMMON_GROUP_CACHE`, `COMMON_GROUP_LIST_CACHE`, `COMMON_DETAIL_CACHE`, `COMMON_DETAIL_LIST_BY_GROUP_CACHE` 상수를 제거합니다.
    - 대신 `CacheNames`를 static import 하여 `COMMON_GROUPS`, `COMMON_GROUP_LIST`, `COMMON_DETAILS`, `COMMON_DETAILS_BY_GROUP` 상수를 사용하도록 변경합니다.
2.  **GlobalSettingsService.java 수정**:
    - 내부의 `SETTING_CACHE` 상수를 제거하고 `CacheNames.SETTINGS`를 사용하도록 변경합니다.

### 장점
*   문자열 오타로 인한 런타임 오류(NPE)를 원천적으로 방지합니다.
*   캐시 설정(`CacheConfiguration`)과 서비스 로직 간의 정합성을 보장합니다.
*   코드의 유지보수성이 향상됩니다.

## 대안 1 (Alternative)
`CommonCodeService` 등에서 사용하는 이름을 기준으로 `CacheConfiguration.java`를 수정합니다.

*   비권장 사유: `CacheNames`라는 통합 관리 클래스가 이미 존재하므로, 이를 따르는 것이 일관성 측면에서 훨씬 유리합니다. 서비스마다 각자의 이름을 고집하면 관리 포인트가 분산됩니다.

---
**이 방향으로 수정을 진행할까요?**
사용자님의 컨펌을 받은 후 상세 구현 계획(Implementation Plan)을 작성하겠습니다.
