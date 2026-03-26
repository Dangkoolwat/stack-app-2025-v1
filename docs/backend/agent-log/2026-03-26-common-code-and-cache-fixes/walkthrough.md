---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

구현 흐름

캐시 정규화
- CommonCodeService.java: 하드코딩된 camelCase 캐시 이름을 제거하고 CacheNames 상수를 사용하도록 변경함.
- GlobalSettingsService.java: 내부 상수 SETTING_CACHE 를 삭제하고 CacheNames.SETTINGS 로 대체함.

중복 코드 차단 로직
- Repository: CommonCodeDetailRepository 에 findOneByGroupGroupCodeAndCode 를 추가하여 전체 데이터(삭제 포함)를 조회하게 함.
- Service: createDetail 과 updateDetail 시 해당 메소드로 기존 데이터를 조회한 뒤, deleted 필드가 true 인 경우 전용 에러(codedeleted)를 발생시킴.
- Localization: ko/global.json 에 관련 한국어 메시지를 추가하여 UI에서 직관적인 안내가 가능하도록 함.

테스트 검증
- CommonCodeServiceT: 단위 테스트를 통해 캐시 NPE 해결 및 중복 코드 차단 조건 작동을 확인하였음.
- GlobalSettingsServiceIT: 통합 테스트를 통해 Redis 캐시 매니저와 정상적으로 통신하여 데이터가 캐싱됨을 확인하였음.
- i18n: 프론트엔드 다국어 파일에 에러 메시지가 올바르게 매핑되었는지 교차 확인함.
