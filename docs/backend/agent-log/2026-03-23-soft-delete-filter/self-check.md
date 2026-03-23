# Self-Check

- [x] Architecture compliance: 기존 JPA/Hibernate 기반 구조 유지, 도메인/리포지토리 동작을 AOP로 보강
- [x] No hidden breaking changes: 기본 조회는 “삭제 제외” 유지(Repository 호출 시 필터 기본 ON)
- [x] Rollback possible: `@Filter`/AOP 제거 시 이전 방식으로 쉽게 회귀 가능
- [x] Test strategy defined: 기존 단위/통합 테스트 컴파일 및 동작 검증, 관리자 시나리오(삭제 포함 조회) 검증
- [x] Security impact reviewed: 기본 노출 경로는 필터 ON으로 보호, 관리자 전용 메서드에만 `@IncludeDeleted` 부여
- [x] Config / dependency impact checked: 신규 의존성 추가 없음, Spring AOP(기존 starter) 사용
- [n/a] Cache safety checked (if used): 조회 대상이 바뀌는 관리자 메서드에만 적용, 기존 캐시 무효화 로직 유지
- [n/a] OpenAPI impact checked (if API changed): API 시그니처 변경 없음

