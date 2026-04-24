---
agent: GPT-5.4
created_at: 2026-03-26 (목)
language: ko
---

# 문제 분석

## 문제 현상

통합 테스트 실행 시 다음 경고가 반복적으로 출력되었다.

- `Reuse was requested but the environment does not support the reuse of containers`

이는 테스트 컨테이너 설정에서 `withReuse(true)`를 요청하고 있지만, 실제 사용자 환경의 `~/.testcontainers.properties`에는 `testcontainers.reuse.enable=true`가 설정되어 있지 않기 때문이다.

## 원인

`src/test/java/com/daangcool/stack/config/IntegrationTestContainers.java`에서 Oracle 및 Redis 컨테이너 모두 `withReuse(true)`를 사용하고 있었다.

하지만 현재 프로젝트는 재사용을 필수 전제로 하지 않으며, 사용자도 재사용을 원하지 않는 상태다.

## 영향

- 테스트 로그에 불필요한 경고가 계속 남는다.
- 다른 개발자가 "환경 설정이 잘못됐다"라고 오해할 수 있다.
- 테스트 종료 후 컨테이너 정리 정책에 대한 이해를 혼동시킬 수 있다.

