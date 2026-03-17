# 2026-03-15 HikariCP + Oracle 연결 풀 설정 최적화

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

HikariCP Oracle DB 실무 권장값 적용 — dev (Docker) / prod 환경별 최적화

---

## Goal

Oracle DB 환경에 맞는 HikariCP 실무 권장값을 dev/prod 프로파일에 적용하고,  
각 파라미터의 의미와 Oracle 특이사항을 문서화한다.

---

## Context

- 기존 dev 환경: `maximum-pool-size` 미설정 (기본값 10), `minimum-idle` 미설정, `keepalive-time: 30000` (30초 — 너무 짧음)
- 기존 prod 환경: `minimum-idle: 5` (dynamic pool), `connection-timeout: 30000` (30초 — 운영에서는 느린 Fail Fast)
- `connection-init-sql` 미설정 → Oracle 세션 NLS/timezone 불일치 위험
- `leak-detection-threshold` 미설정 → 커넥션 누수 감지 불가
- dev 환경은 Docker Oracle 컨테이너 사용 (localhost:1521/FREEPDB1)

---

## Work Performed

1. HikariCP 파라미터 관계 및 Oracle 특이사항 분석
2. `application-dev.yml` HikariCP 섹션 업데이트:
   - `pool-name: HikariPool-Dev` 추가
   - `maximum-pool-size: 10` 명시
   - `minimum-idle: 2` 추가
   - `keepalive-time: 120000` (2분) 으로 조정 — Docker 내부망 특성 반영
   - `connection-init-sql` 추가 — NLS_DATE_FORMAT, TIME_ZONE 고정
3. `application-prod.yml` HikariCP 섹션 업데이트:
   - `pool-name: HikariPool-Prod` 추가
   - `minimum-idle: 20` — fixed-size pool (max와 동일)
   - `connection-timeout: 10000` (10초) — 빠른 Fail Fast
   - `keepalive-time: 300000` (5분) — idle-timeout(10분)의 절반
   - `validation-timeout: 3000` (3초) 추가
   - `leak-detection-threshold: 60000` (60초) 추가
   - `connection-init-sql` 추가 — NLS_DATE_FORMAT, TIME_ZONE, NLS_COMP, NLS_SORT 포함
4. `docs/decisions/2026-03-15-hikaricp-oracle-configuration.md` 작성
5. 본 agent-log 작성

---

## Files Modified

- `src/main/resources/config/application-dev.yml` — HikariCP 설정 최적화
- `src/main/resources/config/application-prod.yml` — HikariCP 설정 최적화
- `docs/decisions/2026-03-15-hikaricp-oracle-configuration.md` — 가이드 문서 (신규)
- `docs/agent-log/2026-03-15-hikaricp-oracle-config.md` — 본 파일 (신규)

---

## 변경 요약

### dev 환경 (Docker Oracle)

| 파라미터 | 변경 전 | 변경 후 | 이유 |
|----------|---------|---------|------|
| `pool-name` | `Hikari` | `HikariPool-Dev` | 모니터링 식별 |
| `maximum-pool-size` | 미설정(10) | `10` 명시 | 명확한 의도 |
| `minimum-idle` | 미설정 | `2` | 최소 연결 유지 |
| `keepalive-time` | `30000` (30s) | `120000` (2m) | Docker 컨테이너 재시작 감지 |
| `connection-init-sql` | 없음 | NLS/TIME_ZONE | Oracle 세션 파라미터 고정 |

### prod 환경 (Oracle XE)

| 파라미터 | 변경 전 | 변경 후 | 이유 |
|----------|---------|---------|------|
| `pool-name` | `Hikari` | `HikariPool-Prod` | 모니터링 식별 |
| `minimum-idle` | `5` | `20` (= max) | fixed-size pool, 안정적 처리량 |
| `connection-timeout` | `30000` (30s) | `10000` (10s) | 빠른 Fail Fast, 스레드 적체 방지 |
| `keepalive-time` | `30000` (30s) | `300000` (5m) | idle-timeout 절반 |
| `validation-timeout` | 없음 | `3000` (3s) | isValid() 타임아웃 명시 |
| `leak-detection-threshold` | 없음 | `60000` (60s) | 커넥션 누수 조기 감지 |
| `connection-init-sql` | 없음 | NLS/TIME_ZONE/NLS_COMP | Oracle 세션 통일 |

---

## Architecture Impact

데이터소스 연결 풀 설정 변경입니다. 애플리케이션 로직, API 계약에는 영향 없습니다.  
`connection-init-sql` 추가로 새 연결 생성 시 세션 파라미터가 자동 설정됩니다.

---

## Security Impact

없음. 연결 풀 성능/안정성 설정입니다.

---

## Verification

```bash
./mvnw spring-boot:run -Pdev
```

애플리케이션 기동 후 아래로 확인합니다:

```
# HikariCP 풀 초기화 로그 확인
grep "HikariPool" logs/dev/application.log

# 연결 풀 상태 확인 (Actuator)
curl -k https://localhost:8443/management/health | jq '.components.db'

# Hikari 메트릭 확인 (Prometheus)
curl -k https://localhost:8443/management/prometheus | grep hikaricp
```

---

## Risks

1. **`connection-init-sql` 실패 시 연결 생성 불가**: SQL 문법 오류 시 풀이 초기화되지 않습니다.  
   YAML 의 `>` 블록 스칼라 형식에서 줄바꿈이 공백으로 치환되므로 ALTER SESSION 구문이 한 줄로 전달됩니다.  
   Oracle 에서 `ALTER SESSION SET a=1 b=2` 형식이 지원되는지 확인이 필요합니다.  
   문제 발생 시 각 파라미터를 세미콜론으로 구분하거나 개별 ALTER SESSION 으로 분리하세요.

2. **prod `connection-timeout: 10000`**: 기존 30초에서 10초로 단축됩니다.  
   DB 부하가 높은 배포 직후 시점에 일시적인 연결 실패가 발생할 수 있습니다.

3. **Docker Oracle 재시작**: dev 환경에서 Oracle 컨테이너 재시작 시 기존 연결이 모두 무효화됩니다.  
   HikariCP 가 자동 복구하지만 재시작 직후 수 초간 연결 오류가 발생할 수 있습니다.

---

## Next Suggested Tasks

- `connection-init-sql` 동작 확인 (실제 기동 테스트)
- Oracle DBA 와 협의하여 운영 방화벽 세션 타임아웃 확인 후 `max-lifetime` 재조정
- Prometheus + Grafana 대시보드에 HikariCP 메트릭 패널 추가 권장:
  - `hikaricp_connections_active`
  - `hikaricp_connections_pending`
  - `hikaricp_connections_timeout_total`

---

## Notes for Future Agents

- dev 환경은 Docker Oracle 컨테이너 (`localhost:1521/FREEPDB1`) 를 사용합니다.
  Docker 내부 네트워크이므로 방화벽 세션 차단이 없어 keepalive 는 컨테이너 재시작 감지 목적입니다.
- prod `minimum-idle = maximum-pool-size = 20` 은 의도적인 fixed-size pool 입니다. 바꾸지 마세요.
- `connection-init-sql` 의 NLS 설정은 Oracle 세션 포맷을 고정합니다.
  `NLS_COMP = LINGUISTIC / NLS_SORT = BINARY_CI` 는 운영만 적용 (한국어 대소문자 무관 검색).
- 상세 가이드는 `docs/decisions/2026-03-15-hikaricp-oracle-configuration.md` 를 참조하세요.
