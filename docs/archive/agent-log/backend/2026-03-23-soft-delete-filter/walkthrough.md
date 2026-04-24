# 구현 워크스루

## 핵심 흐름
1. `domain/board` 패키지에 `@FilterDef(name="softDeleteFilter")`를 선언
2. `Board/Upload/Comment/Tag/BoardTag` 엔티티에 `@Filter(name="softDeleteFilter", condition="is_deleted = 0")`를 적용
3. `SoftDeleteHibernateFilterAspect`가 `com.daangcool.stack.repository..*` 호출을 가로채서 기본적으로 필터를 활성화
4. 관리자 기능처럼 삭제 포함 조회가 필요하면 `@IncludeDeleted`를 서비스 메서드에 붙여 `IncludeDeletedAspect`가 스코프를 열고, 그 구간에서는 필터를 활성화하지 않음

## 중복 제거 포인트
- `nativeQuery`로 `@SQLRestriction`을 우회하던 관리자용 조회를 JPQL로 전환
- “삭제 포함”은 쿼리/리포지토리 분기 대신 `@IncludeDeleted`로 통일

