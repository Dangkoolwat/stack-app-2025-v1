package com.daangcool.stack.config;

import java.time.Duration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * Spring Boot 공식 ImportTestcontainers 패턴을 따르는 통합 테스트 컨테이너 선언부입니다.
 */
public interface IntegrationTestContainers {

    DockerImageName oracleImage = DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart")
        .asCompatibleSubstituteFor("gvenzl/oracle-xe");

    @Container
    @ServiceConnection
    OracleContainer oracle = new OracleContainer(oracleImage)
        .withPassword("oracle")
        .withEnv("ORACLE_PASSWORD", "oracle")
        .withStartupTimeout(Duration.ofSeconds(300));

    @Container
    GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8.0.0"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("jhipster.cache.redis.server", () -> String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379)));
    }
}
