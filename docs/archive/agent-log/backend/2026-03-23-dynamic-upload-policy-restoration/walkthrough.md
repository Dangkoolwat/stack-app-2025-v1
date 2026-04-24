# 구현 흐름 (Walkthrough)

## 마이그레이션 전략
Oracle 환경에서 `MODIFY` 명령으로 `CLOB` 변환이 불가능한 문제를 해결하기 위해 다음과 같은 다단계 SQL을 적용했습니다.

```xml
<changeSet id="20260323173000-1-oracle" author="antigravity" dbms="oracle">
    <sql>ALTER TABLE stack_settings ADD (global_settings_new CLOB)</sql>
    <sql>UPDATE stack_settings SET global_settings_new = global_settings</sql>
    <sql>ALTER TABLE stack_settings DROP COLUMN global_settings</sql>
    <sql>ALTER TABLE stack_settings RENAME COLUMN global_settings_new TO global_settings</sql>
</changeSet>
```

## 역직렬화 최적화
Jackson 3의 엄격한 타입 매핑 정책으로 인해, JSON 필드가 누락되었을 때 기본형(`boolean`, `long`) 매핑 시 발생하는 `MismatchedInputException`을 방지하고자 VO 필드를 래퍼 클래스로 교체했습니다.

- `enabled`: `boolean` -> `Boolean`
- `maxFileSizeBytes`: `long` -> `Long`
- `displayOrder`: `int` -> `Integer`

## 테스트 성공
`SettingsResourceIT`를 통해 Oracle XE 환경에서 스키마 변경, 데이터 저장, 캐시 무효화 및 자동 로딩 로직이 모두 정상 동작함을 확인했습니다 (BUILD SUCCESS).
