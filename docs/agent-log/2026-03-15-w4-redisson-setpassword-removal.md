# 2026-03-15 W-4 Redisson setPassword() deprecated API 제거

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

W-4: Redisson `setPassword()` deprecated API 제거 — URL 기반 패스워드 처리로 전환

---

## Goal

`CacheConfiguration.java` 의 `getRedissonConfig()` 메서드에서 deprecated 된
`setPassword()` API 호출과 `@SuppressWarnings("deprecation")` 어노테이션을 제거한다.
패스워드는 이미 프로퍼티의 Redis URL 에 포함하는 방식으로 설정하고 있으므로,
`setAddress()` / `addNodeAddress()` 가 URL 을 직접 파싱하도록 코드를 단순화한다.

---

## Context

- 기존 코드는 Redis URL 에서 `URI.getUserInfo()` 로 패스워드를 추출해 `setPassword()` 로
  별도 설정하는 2단계 방식이었음.
- `setPassword()` 는 Redisson 4.x 에서 deprecated 처리됨.
- 실제 프로퍼티 설정을 확인한 결과:
  - `application-dev.yml`: `redis://localhost:6379` — 패스워드 없음
  - `application-prod.yml`: `redis://localhost:6379` — 패스워드 없음 (필요 시 URL 에 포함)
- Redisson 은 `redis://:password@host:port` 형식의 URL 을 `setAddress()` 에 전달하면
  내부적으로 파싱하여 인증합니다. `setPassword()` 를 별도로 호출할 필요가 없습니다.
- 따라서 `URI` 파싱 로직과 `setPassword()` 호출을 모두 제거하고,
  프로퍼티 URL 을 그대로 전달하는 방식으로 단순화했습니다.

---

## Work Performed

1. `CacheConfiguration.java` 수정:
   - `import java.net.URI` 제거 (더 이상 사용하지 않음)
   - `@SuppressWarnings("deprecation")` 어노테이션 제거
   - `getRedissonConfig()` 내부:
     - `URI.create()` / `redisUri.getUserInfo()` / `setPassword()` 관련 코드 전체 제거
     - 클러스터 / 단일 서버 모두 URL 을 직접 전달하는 방식으로 단순화
   - Javadoc 에 URL 형식 및 패스워드 포함 방법 설명 추가
   - 클래스 레벨 변경 이력 주석 업데이트 (W-4 해결 명시)
2. 보안 리포트 W-4 항목 완료 처리 (별도 업데이트)
3. 본 agent-log 작성

---

## Files Modified

- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`
  - `import java.net.URI` 제거
  - `@SuppressWarnings("deprecation")` 제거
  - `getRedissonConfig()` 로직 단순화

---

## 변경 전후 비교

### 변경 전

```java
@SuppressWarnings("deprecation")
private Config getRedissonConfig(JHipsterProperties jHipsterProperties) {
    URI redisUri = URI.create(jHipsterProperties.getCache().getRedis().getServer()[0]);
    Config config = new Config();
    config.setCodec(new org.redisson.codec.SerializationCodec());
    if (jHipsterProperties.getCache().getRedis().isCluster()) {
        ClusterServersConfig clusterServersConfig = config
            .useClusterServers()
            ...
            .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());
        if (redisUri.getUserInfo() != null) {
            clusterServersConfig.setPassword(   // deprecated
                redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
        }
    } else {
        SingleServerConfig singleServerConfig = config
            .useSingleServer()
            ...
            .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);
        if (redisUri.getUserInfo() != null) {
            singleServerConfig.setPassword(    // deprecated
                redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
        }
    }
    return config;
}
```

### 변경 후

```java
private Config getRedissonConfig(JHipsterProperties jHipsterProperties) {
    Config config = new Config();
    config.setCodec(new org.redisson.codec.SerializationCodec());
    if (jHipsterProperties.getCache().getRedis().isCluster()) {
        config
            .useClusterServers()
            ...
            .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());
            // redis://:password@host:port 형식이면 Redisson 이 직접 파싱
    } else {
        config
            .useSingleServer()
            ...
            .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);
            // redis://:password@host:port 형식이면 Redisson 이 직접 파싱
    }
    return config;
}
```

---

## 패스워드 설정 가이드

Redis 패스워드가 필요한 경우 `application-prod.yml` 의 서버 URL 에 직접 포함합니다:

```yaml
jhipster:
  cache:
    redis:
      # 패스워드 없음 (현재)
      server: redis://localhost:6379

      # 패스워드 있음 (운영 환경)
      server: redis://:mypassword@localhost:6379

      # 클러스터 + 패스워드
      server: >
        redis://:mypassword@node1:6379,
        redis://:mypassword@node2:6379,
        redis://:mypassword@node3:6379
      cluster: true
```

패스워드를 URL 에 평문으로 넣지 않으려면 환경변수로 주입합니다:

```yaml
jhipster:
  cache:
    redis:
      server: redis://:${REDIS_PASSWORD}@localhost:6379
```

---

## Architecture Impact

`CacheConfiguration` 의 내부 구현만 변경됩니다. `RedissonClient` 빈의 동작은 동일합니다.  
외부 API, 서비스 레이어, 캐시 동작에는 영향이 없습니다.

---

## Security Impact

없음. 패스워드 처리 방식이 동등하게 유지됩니다.  
오히려 `URI.getUserInfo()` 로 패스워드를 문자열로 추출하는 중간 단계가 제거되어  
코드 레벨에서 패스워드 노출 표면이 줄어듭니다.

---

## Verification

```bash
./mvnw clean package -DskipTests   # 컴파일 확인
./mvnw test                        # 단위 테스트 확인
```

Redis 연결 확인은 실행 중인 Redis 인스턴스가 필요합니다:

```bash
./mvnw spring-boot:run -Pdev       # 애플리케이션 기동 후 캐시 동작 확인
```

---

## Risks

없음. Redisson 의 `setAddress()` 가 `redis://:password@host:port` 형식을 공식 지원하며,
기존 코드와 동일한 결과를 반환합니다.

---

## Next Suggested Tasks

W-4 완료. 잔여 보안 항목 중 다음 우선순위:

1. C-1: `application-secret.yml` 자격증명 환경변수화
2. C-2: JWT 시크릿 rotate
3. C-3: prod Oracle 계정 최소 권한 전환
4. C-4: 파일 업로드 MIME 검증 (Apache Tika)
5. W-1: Rate Limiting (Bucket4j)

---

## Notes for Future Agents

- Redis 패스워드는 `jhipster.cache.redis.server` URL 에 `redis://:password@host:port` 형식으로
  포함하면 됩니다. 별도 setPassword() 호출은 필요하지 않습니다.
- 운영 환경에서는 패스워드를 YAML 에 평문으로 넣지 말고 `${REDIS_PASSWORD}` 환경변수로 주입하세요.
- `import java.net.URI` 는 이 파일에서 더 이상 사용하지 않으므로 다시 추가하지 마세요.
