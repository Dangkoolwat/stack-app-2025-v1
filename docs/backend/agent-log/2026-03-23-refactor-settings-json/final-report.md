# 최종 보고서 (Final Report)

## 요약
`Settings` 엔티티의 글로벌 설정 저장 방식을 개별 컬럼 방식에서 단일 JSON 필드(`CLOB`) 방식으로 성공적으로 리팩토링했습니다.

## 변경 사유
- 설정 항목 증가에 따른 빈번한 DB 스키마 변경 방지
- 데이터 저장 구조의 유연성 확보 및 관리 포인트 단일화

## 영향 및 결과
- DB: `stack_settings` 테이블이 `id`, `global_settings` (VARCHAR), `description` (VARCHAR) 구조로 최적화되었습니다.
- 성능: `@Lob` 제거 및 `CacheWarmupRunner`를 통한 초기 캐시 로딩으로 성능과 리소스 효율이 개선되었습니다.
- 유지보수: 새로운 설정 추가 시 Java 코드(DTO, Entity helper)만 수정하면 되며, 중요 설명(`description`)은 별도 컬럼으로 관리되어 가독성이 높습니다.

## 검증 결과
- Liquibase를 통한 초기 테이블 생성 및 데이터 주입 확인
- 엔티티 내 JSON 파싱 및 델리게이트 메서드 정상 동작 확인 (`SettingsTest` 통과)
- 서비스 레벨 통합 테스트 (`GlobalSettingsServiceIT` 통과)
- 기동 시 캐시 워밍업 로직 정상 동작 확인 (Liquibase 초기화 모드 대응 완료)
