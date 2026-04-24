# 최종 보고

## 요약
- `@SQLRestriction("is_deleted = 0")` 기반 고정 필터링을 Hibernate `@Filter` 기반 “토글 가능” 구조로 변경했습니다.
- 기본 조회는 `softDeleteFilter`를 AOP로 자동 활성화하여 기존 동작(삭제 제외)을 유지했습니다.
- 관리자/운영 조회는 `@IncludeDeleted`로 필터를 비활성화해 삭제 데이터를 포함할 수 있게 했고, 우회용 네이티브 쿼리를 JPQL로 정리했습니다.

## 이유
- 관리자 기능에서만 삭제 데이터 포함이 필요하므로, 엔티티 레벨 고정 조건 대신 요청/메서드 단위 토글이 가능한 방식이 중복을 줄입니다.

## 영향
- 일반 API: 삭제 데이터 자동 제외 유지
- 관리자 기능: `@IncludeDeleted`가 붙은 기능에서 삭제 데이터 조회/복구/하드 삭제가 가능
- 기존 네이티브 쿼리 의존도 감소

## 결과
- 전체 테스트 실행 완료:
  - 실행: `export $(cat .env | grep -v '^#' | xargs) && ./mvnw clean test`
  - 결과: Tests run 70, Failures 0, Errors 0, Skipped 0 (BUILD SUCCESS)
  - 비고: 최초 실행 시 Testcontainers 이미지 pull(Oracle/Redis)로 시간이 오래 걸릴 수 있음(amd64 이미지 emulation 경고 포함)
