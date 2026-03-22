## 구현 단계
1. **문제의 원리 분석**: 에러 로그 및 프론트엔드 구성 요소 코드를 점검하여 ID 생성 책임이 JPA의 시퀀스(`common_detail_sequence_generator`)에 국한되어 있음을 파악.
2. **원인 지점 식별**: `202510081910000_create_common_code_tables.xml`에서 시퀀스의 `startValue=1`로 되어 있음. 반면 같은 로그에서 `stack_common_code_detail` 테이블에 초기 더미 값(ID 1~21)을 인서트함.
3. **해결 및 대응**:
   - `startValue`를 `1000`으로 갱신하여 신규 배포 시나리오를 고침.
   - 사용자/관리자는 기존 로컬 DB에서 `ALTER SEQUENCE common_detail_sequence_generator RESTART WITH 1000;` 쿼리를 실행하거나 DB를 재초기화하게끔 안내함.

## 변경 대상 파일 목록
- `src/main/resources/config/liquibase/changelog/202510081910000_create_common_code_tables.xml`

## 보조 테스트 전략
개발 환경 초기화 후, 어드민 UI의 [Add Detail Code]를 통해 `TEST_CODE` 인서트 확인, DB상에 ID 1000으로 적재되는지 단위 또는 수동 테스트 수행.
