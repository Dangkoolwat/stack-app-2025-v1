# HikariCP + Oracle DB 연결 풀 설정 가이드

프로젝트: `stack-app-2025-v1`  
DB: Oracle (dev: Docker 컨테이너, prod: Oracle XE)  
작성일: 2026-03-15  
적용 파일: `application-dev.yml`, `application-prod.yml`

---

## 개요

HikariCP 는 Java 생태계에서 가장 빠른 JDBC 커넥션 풀입니다.  
Oracle DB 환경에서는 방화벽 세션 차단, NLS 포맷 불일치, 세션 타임아웃 등 특유의 이슈가 있으므로  
일반적인 MySQL/PostgreSQL 설정과 다른 접근이 필요합니다.

---

## 핵심 원칙

### 1. 파라미터 간 필수 관계

```
connectionTimeout < keepaliveTime < idleTimeout < maxLifetime < Oracle 세션 타임아웃
```

이 순서가 깨지면 연결이 풀에서 제거되기 전에 Oracle 서버에서 먼저 끊기거나,
반대로 죽은 연결이 풀에 남아 `ORA-17008 Closed Connection` 오류가 발생합니다.

### 2. fixed-size pool (운영 권장)

`minimum-idle = maximum-pool-size` 로 설정하면 풀 크기가 고정됩니다.  
연결 생성/제거 오버헤드가 없어 처리량이 안정적입니다. HikariCP 공식 권고 방식입니다.

### 3. Oracle 세션 타임아웃 확인 필수

운영 DBA 에게 아래 값을 확인하고 `maxLifetime` 을 그보다 짧게 설정해야 합니다:

```sql
-- Oracle User Profile 확인
SELECT resource_name, limit
FROM dba_profiles
WHERE profile = 'DEFAULT'
  AND resource_name IN ('IDLE_TIME', 'CONNECT_TIME');

-- 방화벽 설정 확인 (DBA 문의 필요)
-- SQLNET.EXPIRE_TIME 값 (sqlnet.ora)
```

| Oracle 세션 타임아웃 | maxLifetime 권장값 |
|---------------------|-------------------|
| 60분 이상 (기본값)   | 30분 (1800000ms)  |
| 30분                 | 25분 (1500000ms)  |
| 15분 (방화벽 차단)   | 14분 (840000ms)   |

---

## 풀 크기 산정 공식

HikariCP 공식 권고 공식입니다:

```
pool size = (CPU 코어 수 × 2) + 유효 스핀들 수
```

| 서버 사양 | 계산 | 권장값 |
|-----------|------|--------|
| 4코어     | 4×2+1=9  | 10 |
| 8코어     | 8×2+1=17 | 20 |
| 16코어    | 16×2+1=33 | 30~40 |

주의: 여러 애플리케이션 인스턴스가 같은 Oracle DB 를 공유하는 경우:
```
Oracle 총 허용 세션 수 ≥ 인스턴스 수 × maximum-pool-size
```

Oracle SESSIONS 파라미터 확인:
```sql
SHOW PARAMETER SESSIONS;
-- 기본값: SESSIONS = PROCESSES × 1.1 + 5
```

---

## 파라미터 상세 설명

### `pool-name`
모니터링 도구(JMX, Micrometer, Prometheus)에서 풀을 식별하는 이름입니다.  
환경별로 다르게 설정하면 로그 분석이 편합니다.

### `auto-commit: false`  중요
JPA/Hibernate 가 트랜잭션을 직접 제어하므로 반드시 `false` 입니다.  
`true` 로 설정하면 `@Transactional` 이 제대로 동작하지 않습니다.

### `maximum-pool-size`
최대 동시 연결 수입니다. Oracle 라이선스 및 DB 서버 SESSIONS 파라미터 범위 내에서 설정합니다.

### `minimum-idle`
유휴 상태로 유지할 최소 연결 수입니다.  
- `minimum-idle < maximum-pool-size`: 동적 풀 (부하에 따라 연결 증감)  
- `minimum-idle = maximum-pool-size`: fixed-size 풀 (운영 권장)

### `connection-timeout`
`getConnection()` 호출 후 풀에서 연결을 기다리는 최대 시간입니다.  
초과 시 `SQLTransientConnectionException` 이 발생합니다.

- 운영에서 너무 길면 (예: 30초) 요청 스레드가 쌓여 장애가 악화됩니다.
- 운영 권장: 10초 — 빠른 장애 감지와 빠른 실패(Fail Fast)

### `idle-timeout`
`minimum-idle < maximum-pool-size` 일 때만 동작합니다.  
초과 유휴 연결을 풀에서 제거하는 시간입니다.  
fixed-size 풀에서는 무시되지만 설정은 유지합니다.

### `max-lifetime`
연결이 생성된 후 이 시간이 지나면 다음 획득 요청 시 폐기하고 새 연결을 생성합니다.  
Oracle 서버의 세션 타임아웃보다 반드시 짧게 설정해야 합니다.  
실제 폐기 시점은 `max-lifetime - 500ms ~ max-lifetime` 사이 무작위 값입니다 (thundering herd 방지).

### `keepalive-time`
유휴 연결에 주기적으로 ping 을 보내 Oracle 방화벽/세션 차단을 방지합니다.  
Oracle 환경에서는 필수 설정입니다.

ping 방법 (우선순위):
1. JDBC4 `Connection.isValid()` — ojdbc17 지원, 별도 설정 불필요
2. `connection-test-query: SELECT 1 FROM DUAL` — 명시적 쿼리

`keepalive-time < idle-timeout` 권장 (유휴 연결이 ping 대상이 되도록)

### `validation-timeout`
`isValid(timeout)` 에 전달되는 타임아웃 값(밀리초)입니다.  
Oracle 에서는 3초로 설정합니다.

### `leak-detection-threshold`
이 시간(밀리초) 이상 반환되지 않은 연결을 WARN 로그로 출력합니다.  
값이 `0` 이면 비활성화됩니다.  
설정 기준: 가장 오래 걸리는 정상 트랜잭션 시간 × 2 이상으로 설정합니다.

### `connection-init-sql`
새 연결 생성 직후 Oracle 세션 파라미터를 고정합니다.  
풀의 모든 연결이 동일한 세션 설정을 갖도록 보장합니다.

```sql
ALTER SESSION SET
  NLS_DATE_FORMAT     = 'YYYY-MM-DD HH24:MI:SS'
  NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS.FF3'
  TIME_ZONE           = 'Asia/Seoul'
  NLS_COMP            = LINGUISTIC    -- 운영만, 대소문자 무관 검색
  NLS_SORT            = BINARY_CI     -- 운영만, 한국어 정렬 통일
```

NLS_DATE_FORMAT 을 설정하는 이유:  
Oracle 기본값은 `DD-MON-RR` 이며, Hibernate 가 날짜를 문자열로 바인딩할 때  
Oracle 클라이언트 로케일에 따라 포맷이 달라져 `ORA-01843 invalid month` 오류가 발생할 수 있습니다.

TIME_ZONE 을 설정하는 이유:  
Hibernate 의 `Instant` → `TIMESTAMP WITH TIME ZONE` 매핑 시  
DB 세션 timezone 과 애플리케이션 timezone 이 다르면 1~9시간 차이가 발생합니다.

---

## 환경별 설정 값 비교

| 파라미터 | dev (Docker) | prod (Oracle XE) | 차이 이유 |
|----------|-------------|-----------------|-----------|
| `pool-name` | `HikariPool-Dev` | `HikariPool-Prod` | 로그 식별 |
| `maximum-pool-size` | `10` | `20` | dev 는 단일 개발자 용도 |
| `minimum-idle` | `2` | `20` (fixed) | 운영은 연결 생성 오버헤드 제거 |
| `connection-timeout` | `30000` (30s) | `10000` (10s) | 운영: 빠른 Fail Fast |
| `idle-timeout` | `600000` (10m) | `600000` (10m) | 동일 |
| `max-lifetime` | `1800000` (30m) | `1800000` (30m) | 동일 |
| `keepalive-time` | `120000` (2m) | `300000` (5m) | dev: Docker 내부망, 빠른 재확인 |
| `validation-timeout` | 미설정 | `3000` (3s) | 운영만 명시 |
| `leak-detection-threshold` | 미설정 | `60000` (60s) | 운영 누수 감지 |
| `NLS_COMP/SORT` | 미설정 | 설정 | 운영 한국어 정렬 통일 |

---

## dev 환경 — Docker Oracle 특이사항

Docker 컨테이너에서 Oracle 을 사용할 때의 주의사항:

### 컨테이너 재시작 시 연결 무효화

Docker 컨테이너가 재시작되면 기존 풀의 모든 연결이 즉시 무효화됩니다.  
HikariCP 는 연결 오류 발생 시 자동으로 새 연결을 생성하지만,  
일시적으로 `ORA-17002 I/O Exception` 또는 `Connection is closed` 오류가 발생할 수 있습니다.

해결: 애플리케이션을 재시작하거나 잠시 기다리면 자동 복구됩니다.

### Docker 내부 네트워크에서의 keepalive

Docker 내부 네트워크는 방화벽 세션 타임아웃이 없으므로  
dev 환경의 `keepalive-time: 120000` (2분) 은 주로  
Oracle 컨테이너 재시작 감지 목적으로 사용됩니다.

### Docker Compose 사용 시 URL 형식

```yaml
# application-dev.yml
spring:
  datasource:
    # 서비스 이름 방식 (권장)
    url: jdbc:oracle:thin:@//localhost:1521/FREEPDB1
    # SID 방식 (구형)
    # url: jdbc:oracle:thin:@localhost:1521:XE
```

### Oracle Docker 이미지별 기본 PDB

| 이미지 | 기본 PDB |
|--------|---------|
| `container-registry.oracle.com/database/free:latest` | `FREEPDB1` |
| `container-registry.oracle.com/database/express:21.3.0-xe` | `XEPDB1` |
| `gvenzl/oracle-xe:21-slim` | `XEPDB1` |
| `gvenzl/oracle-free:23-slim` | `FREEPDB1` |

---

## Oracle Wallet / SSL 연결 (OCI 운영 환경)

OCI (Oracle Cloud Infrastructure) 의 Autonomous Database 또는 TLS 필수 환경에서는  
다음과 같이 URL 을 변경합니다:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@${DB_TNS_NAME}?TNS_ADMIN=${WALLET_PATH}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      # Wallet 연결은 TLS 핸드셰이크 시간이 있으므로 타임아웃을 약간 늘립니다
      connection-timeout: 20000
```

---

## 모니터링 — Micrometer + Prometheus

HikariCP 는 Micrometer 와 자동 통합됩니다.  
Prometheus 로 수집되는 주요 메트릭:

| 메트릭 | 설명 | 경보 기준 |
|--------|------|----------|
| `hikaricp_connections_active` | 현재 사용 중인 연결 수 | `maximum-pool-size` 의 80% 이상 지속 |
| `hikaricp_connections_pending` | 연결 대기 중인 스레드 수 | 1 이상 지속 시 풀 부족 |
| `hikaricp_connections_timeout_total` | 타임아웃으로 실패한 연결 요청 수 | 증가 추세 시 풀 크기 부족 |
| `hikaricp_connection_acquire_ms` | 연결 획득 소요 시간 | `connection-timeout` 의 50% 이상 |
| `hikaricp_connections` | 전체 연결 수 (active + idle) | `maximum-pool-size` 와 일치해야 정상 |

Actuator 엔드포인트로도 확인 가능합니다:
```
GET /management/health  → 연결 풀 상태 포함
```

---

## 장애 대응 가이드

### ORA-17008: Closed Connection

원인: 풀에 남아있는 연결이 Oracle 서버에서 이미 끊긴 상태  
조치:
1. `keepalive-time` 을 낮춥니다 (예: 300000 → 120000)
2. `max-lifetime` 을 Oracle 방화벽 타임아웃보다 짧게 설정합니다
3. Oracle DBA 에게 `IDLE_TIME` 프로파일 값을 확인합니다

### SQLTransientConnectionException: Connection is not available

원인: `connection-timeout` 초과 — 풀이 가득 찬 상태  
조치:
1. `hikaricp_connections_active` 메트릭을 확인합니다
2. `maximum-pool-size` 를 증가시킵니다 (Oracle 허용 범위 내)
3. 느린 쿼리를 찾아 최적화합니다 (`hikaricp_connection_acquire_ms` 확인)
4. `leak-detection-threshold` 를 활성화해 누수 여부를 확인합니다

### ORA-01843: not a valid month (날짜 오류)

원인: `connection-init-sql` 미설정 또는 Oracle 클라이언트 NLS 불일치  
조치: `connection-init-sql` 에 `NLS_DATE_FORMAT` 설정 확인

### 연결 누수 (leak-detection 경고)

원인: 트랜잭션이 완료되지 않거나 `Connection` 이 수동으로 닫히지 않음  
조치:
1. 경고 로그에 출력되는 스택 트레이스를 분석합니다
2. `@Transactional` 어노테이션 누락 여부를 확인합니다
3. `try-with-resources` 또는 `JdbcTemplate` 사용 여부를 확인합니다

---

## 관련 문서

- `docs/agent-log/2026-03-15-hikaricp-oracle-config.md` — 설정 적용 작업 로그
- `src/main/resources/config/application-dev.yml` — dev 환경 적용값
- `src/main/resources/config/application-prod.yml` — prod 환경 적용값
- HikariCP 공식 문서: https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby
- Oracle JDBC 공식 문서: https://docs.oracle.com/en/database/oracle/oracle-database/23/jjdbc/
