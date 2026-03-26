---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 구현 계획

## 단계

1. 현재 `pom.xml`과 테스트 설정을 분석한다.
2. `verify`에서 `*IT`가 실행되도록 Failsafe를 추가한다.
3. Testcontainers 선언 방식을 `ImportTestcontainers` 기반으로 바꾼다.
4. 대표 관리자 IT를 JWT Bearer 토큰 방식으로 전환한다.
5. 테스트 규칙 문서를 실제 계약에 맞게 보강한다.
6. 테스트 컴파일과 대표 IT를 실행해 결과를 확인한다.
7. 결과를 agent-log와 knowledge item으로 기록한다.

## 변경 파일

- `pom.xml`
- `src/test/java/com/daangcool/stack/IntegrationTest.java`
- `src/test/java/com/daangcool/stack/config/IntegrationTestContainers.java`
- `src/test/java/com/daangcool/stack/config/TestcontainersConfiguration.java`
- `src/test/java/com/daangcool/stack/web/rest/BoardAdminResourceIT.java`
- `src/test/java/com/daangcool/stack/web/rest/TagAdminResourceIT.java`
- `src/test/resources/logback.xml`
- `AGENTS.md`
- `docs/operations/testing-guideline.md`
- `docs/knowledge/2026-03-26-spring-boot4-test-execution-contract.md`

## 테스트 계획

- `./mvnw -q -DskipTests test-compile`
- `export $(grep -v '^#' .env | xargs) && ./mvnw -q -Dskip.installnodenpm -Dskip.npm -Dit.test=TagAdminResourceIT,BoardAdminResourceIT verify`

