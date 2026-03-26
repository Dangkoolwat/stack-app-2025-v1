---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 구현 계획

## 단계

1. `IntegrationTestContainers.java`에서 재사용 설정 위치를 확인한다.
2. Oracle/Redis 컨테이너의 `withReuse(true)`를 제거한다.
3. 테스트 소스 컴파일로 설정 유효성을 확인한다.
4. 작업 결과를 backend agent-log에 기록한다.

## 변경 파일

- `src/test/java/com/daangcool/stack/config/IntegrationTestContainers.java`
- `docs/backend/agent-log/2026-03-26-remove-testcontainers-reuse/`

## 검증 계획

- `./mvnw -q -DskipTests test-compile`

