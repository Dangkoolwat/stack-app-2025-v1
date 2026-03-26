---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 셀프 체크

## 정확성

- [x] Oracle 테스트 이미지가 `oracle-free` 계열로 교체되었는가
- [x] `asCompatibleSubstituteFor`로 Testcontainers 호환 선언을 추가했는가
- [x] Oracle Free 기본 PDB와 충돌하는 `withDatabaseName(...)`를 제거했는가

## 검증

- [x] `./mvnw -q -DskipTests test-compile` 통과
- [x] `export $(grep -v '^#' .env | xargs) && ./mvnw -q -Dskip.installnodenpm -Dskip.npm -Dit.test=TagAdminResourceIT,BoardAdminResourceIT verify` 통과
- [x] `target/failsafe-reports`에서 2개 IT, 총 9개 테스트 통과 확인

## 리스크

- [ ] 전체 통합 테스트 스위트 전체 재실행
- [ ] Oracle Free 전환이 다른 Oracle 전용 테스트 케이스에도 동일하게 안정적인지 추가 확인

