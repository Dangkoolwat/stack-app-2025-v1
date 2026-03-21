# 해결 방안 제안 (Proposal)

## 제안 방향
- CPU 코어 수 기반의 HikariCP 풀 사이즈 자동 계산 로직 추가.
- `DatabaseConfiguration`에서 런타임 CPU 코어 수를 감지해 자동으로 풀 사이즈를 설정.

## 선택 이유 및 기대 효과
- **유연성 확보**: `application-dev.yml` 및 `application-prod.yml`에서 `max-pool-size: 0`으로 설정할 경우, `(Runtime.getRuntime().availableProcessors() * 2) + 1` 공식을 기반으로 유동적으로 할당하도록 함.
- 또한 `minimum-idle: -1`로 설정할 경우 자동으로 `maximumPoolSize`와 일치시킴으로써 고정 풀을 운용해 오버헤드 방지.
- 개발과 운영의 환경 차이에 자동으로 적응하며, 필요할 때는 하드코딩(예: `15`)이 가능하도록 하위 호환성 유지.
