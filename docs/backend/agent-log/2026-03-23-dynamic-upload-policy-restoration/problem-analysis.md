# 문제 분석 (Problem Analysis)

## 현상
- Liquibase 마이그레이션 중 `ORA-12899: value too large for column` 에러 발생.
- `global_settings` 컬럼의 크기(VARCHAR2(2000))가 기본 템플릿 데이터를 수용하기에 부족함.
- `ALTER TABLE ... MODIFY CLOB` 시도 시 `ORA-22858: invalid alteration of datatype` 에러 발생.
- 통합 테스트(`SettingsResourceIT`) 실패.
- Jackson 3 환경에서 JSON 역직렬화 시 `MismatchedInputException` (null to primitive boolean) 발생.

## 원인
1. 데이터 크기 초과: 초기 설계된 `VARCHAR2(2000)` 용량을 초과하는 대량의 템플릿 데이터가 `global_settings`에 포함됨.
2. Oracle 제약 사항: Oracle 데이터베이스는 `ALTER TABLE MODIFY`를 통한 `VARCHAR2` -> `CLOB` 직접 변환을 제한함.
3. Jackson 3 엄격함: `FileTypePolicy` VO의 `enabled` 필드가 기본 `boolean` 타입으로 선언되어, JSON에 필드가 누락되었을 때 `null` 매핑 실패.

## 영향
- 신규 설치 및 마이그레이션 환경에서 애플리케이션 기동 불가.
- 관리자 설정 화면에서 템플릿 기능을 정상적으로 사용할 수 없음.
- 템플릿 데이터 소실로 인한 사용자 편의성 저하.
