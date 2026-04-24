# 제안

## 방안 A (채택): Hibernate `@Filter`로 soft delete 기본 적용 + 관리자만 예외 처리
- `@SQLRestriction("is_deleted = 0")`를 제거하고, 엔티티에 `@Filter(name="softDeleteFilter", condition="is_deleted = 0")`를 적용
- Repository 호출 시점에 AOP로 필터를 기본 활성화하여 기존 “기본 API는 삭제 제외” 동작을 유지
- 관리자/운영 기능에서 삭제 포함 조회가 필요한 메서드에 `@IncludeDeleted`를 부여해 필터를 비활성화(삭제 데이터 포함)

### 선택 이유
- “기본은 안전(삭제 제외)”을 강하게 유지하면서도 “관리자만 예외(삭제 포함)”를 요청/메서드 단위로 제어 가능
- 네이티브 쿼리/별도 관리자 리포지토리 중복을 줄일 수 있음
- 적용 범위가 명확하고 롤백이 용이함(필터/AOP 제거 시 원상 복귀)

### 리스크
- 필터 활성화 로직(AOP)이 누락되면 삭제 데이터가 노출될 수 있으므로 “Repository 호출 시 기본 활성화”를 강제하는 형태로 구현 필요
- `@IncludeDeleted` 적용 범위가 과도하면 관리자 기능에서 의도치 않게 삭제 데이터까지 섞여 조회될 수 있으므로 대상 메서드를 제한적으로 지정해야 함

## 방안 B: Repository 공통 조건 주입(명시적 where 추가)
- 모든 조회에 `deleted=false` 조건을 명시적으로 주입(예: Specification/Querydsl 공통 predicate)
- 장점: Hibernate 기능 의존도 감소
- 단점: 누락 시 노출 리스크가 커지고, 기존 코드 전반을 광범위하게 수정해야 할 가능성이 큼

