# 최종 보고서 (Final Report)

## 요약
- Liquibase 마이그레이션 에러(ORA-12899, ORA-22858)를 완전히 해결하고, 사용자 요청에 따른 대용량 파일 업로드 템플릿 복원 기능을 완성했습니다.
- Jackson 3 환경에서의 타입 매핑 안정성을 강화했습니다.

## 결과물
- 변경 파일: 
    - `20251005203000_added_entity_Settings.xml`
    - `20260323173000_increase_settings_size.xml`
    - `FileTypePolicy.java`
    - `UploadService.java`
    - `SettingsResourceIT.java`
    - `README.md`
- 검증 결과: 
    - Oracle XE 통합 테스트 통과.
    - API 응답 JSON 정합성 확인.

## 영향 및 향후 과제
- 이제 대용량 JSON 데이터(설정)를 안전하게 관리할 수 있는 컬럼 구조가 확보되었습니다.
- 향후 추가적인 템플릿(예: PSD 전문 템플릿 등)을 추가할 때도 스키마 변경 없이 `global_settings` JSON 데이터만 업데이트하면 됩니다.
