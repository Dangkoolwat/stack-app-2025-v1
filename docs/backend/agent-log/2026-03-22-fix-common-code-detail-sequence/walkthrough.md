## 구현 흐름 분석
1. 에러 원문 파악: `ORA-00001` 제약조건(PK_STACK_COMMON_DETAIL) 위반, 충돌 값은 `ID: 3`. 이는 시퀀스가 채번한 ID 값이 현재 테이블에 수동으로 적재된 데이터(1~21)와 충돌함을 강하게 암시.
2. 시퀀스 전략 및 엔티티 확인:
   - `CommonCodeDetail.java`의 `@SequenceGenerator` (`common_detail_sequence_generator`, 로케이션: `GenerationType.SEQUENCE`)을 확인.
   - 프론트엔드(`common-code-detail-update.vue`)에서는 `id` 컬럼을 입력조차 불가능한 `readonly` 상태로 관리함을 검증 완료 (엔드유저의 인덱스 수동 입력 문제가 결코 아님).
3. Liquibase changelog 점검: 
   - `202510081910000_create_common_code_tables.xml`에서 시퀀스의 `startValue="1"`을 발견.
   - 이후 같은 스크립트에서 샘플 행을 다수 `insert` (ID를 최대 21까지 사용).
4. 수정 및 배포:
   - `startValue`를 향후 더 큰 스케일로 충돌이 없도록 `1000`으로 갱신하는 것이 올바름. (`1` -> `1000`)
   - 실 운용 환경에서는 이미 시퀀스가 생성되어 Liquibase checksum 또는 구조 갱신이 일어나지 않으므로, 사용자 구두 가이드를 추가하여 시퀀스 변경 쿼리를 안내하거나 개발 환경에서는 초기화가 더 쉬움을 권장하기로 결론.
