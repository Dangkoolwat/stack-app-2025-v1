---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

구현 계획

단계 1. 캐시 상수 단일화
- CommonCodeService, GlobalSettingsService 내부의 하드코딩된 캐시 이름을 CacheNames 상수로 교체.
- 테스트 코드(GlobalSettingsServiceT)에서도 상수 사용하도록 수정.

단계 2. 중복 코드 차단 로직 강화
- CommonCodeDetailRepository 에 findOneByGroupGroupCodeAndCode 메소드 추가 (삭제 필터 없이 조회).
- CommonCodeService 의 create/update 로직에서 조회 결과 존재 시 활성/삭제 상태를 구분하여 에러 발생.

단계 3. 다국어 메시지 추가
- ko/global.json, en/global.json 에 중복 코드(groupdeleted, codedeleted) 관련 메시지 정의.

단계 4. 검증
- Unit Test 실행하여 로직 검증.
- 통합 테스트 실행하여 캐시 정상 동작 확인.
