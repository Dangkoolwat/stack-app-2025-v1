# 문제 분석

## 문제 현상
- `domain/board` 계열 엔티티에 `@SQLRestriction("is_deleted = 0")`가 적용되어 있어 기본 조회에서는 삭제된 데이터가 자동 제외됨
- 관리자 기능(삭제 목록 조회, 복구, 하드 삭제, 감사/운영 조회 등)에서 `is_deleted = 1` 데이터까지 포함해 조회해야 하는데, 동일한 API/서비스 로직을 재사용하기 어렵고 `nativeQuery`/별도 Repository가 늘어나는 경향이 발생

## 재현/관찰
- `Board`, `Upload`, `Comment`, `Tag`, `BoardTag` 엔티티에 `@SQLRestriction("is_deleted = 0")` 적용
- 관리자 기능을 위해 Repository에 `nativeQuery = true` 조회 메서드가 추가되어 중복이 발생(삭제 포함/삭제만 조회 등)

## 원인
- `@SQLRestriction`는 엔티티 매핑 수준에서 고정 SQL 조건을 주입하므로, JPQL/Query Method로는 조건을 “해제”하거나 “요청 단위로 토글”하기 어려움
- 그 결과 관리자 케이스에서 동일한 조회 로직을 재사용하려면 우회(네이티브 쿼리/별도 리포지토리)를 추가해야 함

## 영향
- 관리자 기능 구현 시 중복 쿼리/리포지토리 증가
- 정책 변경(soft delete 조건 변경) 시 변경 범위가 커지고 실수 가능성 증가
- 테스트/운영 도구에서 삭제 데이터 조회가 어려워짐

