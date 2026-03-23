# 구현 계획

## 단계
1. `domain/board` 엔티티의 `@SQLRestriction("is_deleted = 0")` 제거
2. Hibernate `@Filter`/`@FilterDef`로 soft delete 조건 정의 및 적용
3. Repository 호출 시 `softDeleteFilter`를 기본 활성화하는 AOP 추가
4. 관리자/운영 메서드에 `@IncludeDeleted`를 부여해 “삭제 포함” 조회 경로를 표준화
5. 기존 네이티브 우회 쿼리를 JPQL로 전환(중복 제거)
6. 테스트 실행 및 영향 확인

## 변경 파일(예상)
- `src/main/java/com/daangcool/stack/domain/board/*.java`
- `src/main/java/com/daangcool/stack/domain/board/package-info.java`
- `src/main/java/com/daangcool/stack/config/hibernate/*`
- `src/main/java/com/daangcool/stack/repository/board/*Repository.java`
- `src/main/java/com/daangcool/stack/service/board/*Service.java`
- `src/test/java/...` 일부(필터 토글에 따른 검증 방식 조정)

## 테스트
- `mvn test`

