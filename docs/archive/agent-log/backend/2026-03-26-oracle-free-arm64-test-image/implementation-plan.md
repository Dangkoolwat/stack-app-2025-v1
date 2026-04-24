---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 구현 계획

## 단계

1. 현재 Oracle 테스트 이미지와 데이터베이스명 설정을 확인한다.
2. `oracle-free` 이미지로 교체한다.
3. `OracleContainer` 호환 대체 선언을 추가한다.
4. Oracle Free 기본 PDB 생성과 충돌하는 설정을 제거한다.
5. 테스트 컴파일과 대표 IT를 다시 실행한다.
6. 결과를 agent-log로 기록한다.

## 변경 파일

- `src/test/java/com/daangcool/stack/config/IntegrationTestContainers.java`
- `docs/backend/agent-log/2026-03-26-oracle-free-arm64-test-image/`

## 검증 계획

- `./mvnw -q -DskipTests test-compile`
- `export $(grep -v '^#' .env | xargs) && ./mvnw -q -Dskip.installnodenpm -Dskip.npm -Dit.test=TagAdminResourceIT,BoardAdminResourceIT verify`

