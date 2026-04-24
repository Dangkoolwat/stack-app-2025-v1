# 구현 계획 (Implementation Plan)

## 1단계: Liquibase 스키마 및 데이터 수정
- [x] `20251005203000_added_entity_Settings.xml`: `global_settings` 타입을 `clob`으로 변경 및 초기 데이터 크기 조정.
- [x] `20260323173000_increase_settings_size.xml`: Oracle DBMS 유형에 따른 조건부 SQL 작성 (ADD -> UPDATE -> DROP -> RENAME).
- [x] `master.xml`: 변경 내역 순서 재조정 (스키마 확장 후 데이터 업데이트).

## 2단계: 백엔드 도메인 및 서비스 수정
- [x] `FileTypePolicy.java`: `boolean enabled` -> `Boolean enabled` 등 래퍼 타입으로 변경.
- [x] `UploadService.java`: `isEnabled()` 메서드 참조를 `Boolean.TRUE.equals(getEnabled())` 방식으로 수정 (컴파일 에러 해결).
- [x] `GlobalSettingsService.java`: 캐시 변수 접근 권한 및 로직 복구.

## 3단계: 검증 및 문서화
- [x] `SettingsResourceIT`: 통합 테스트 실행 및 Oracle DB 컨테이너 환경 검증.
- [x] `README.md`: 동적 정책 및 템플릿 복원 기능 설명 추가.
