package com.daangcool.stack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * Redis Testcontainer 설정 클래스
 * ---------------------------------------------------
 * - Redis 8.0.0 컨테이너를 테스트 환경에서 실행
 * - getHost() / getMappedPort(6379) 으로 접근 가능
 * - GenericContainer<?> 명시 → unchecked 경고 제거
 */
public class RedisTestContainer implements InitializingBean, DisposableBean {

    private GenericContainer<?> redisContainer;  // ✅ 제네릭 명시
    private static final Logger LOG = LoggerFactory.getLogger(RedisTestContainer.class);

    @Override
    public void destroy() {
        if (redisContainer != null && redisContainer.isRunning()) {
            redisContainer.close();
            LOG.info("RedisTestContainer stopped.");
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (redisContainer == null) {
            redisContainer = new GenericContainer<>("redis:8.0.0")
                .withExposedPorts(6379)
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withReuse(true);
        }

        if (!redisContainer.isRunning()) {
            redisContainer.start();
            LOG.info("RedisTestContainer started at {}:{}", redisContainer.getHost(), redisContainer.getMappedPort(6379));
        }
    }

    public GenericContainer<?> getRedisContainer() {  // ✅ 제네릭 일치
        return redisContainer;
    }
}
