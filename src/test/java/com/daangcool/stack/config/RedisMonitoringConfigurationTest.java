package com.daangcool.stack.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisMaster;
import org.redisson.api.redisnode.RedisSingle;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RedisMonitoringConfigurationTest {

    private RedissonClient redissonClient;
    private RedisMonitoringConfiguration configuration;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        configuration = new RedisMonitoringConfiguration();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void redisMetrics_ShouldRegisterUsedMemoryGauge() {
        // Given
        var binder = configuration.redisMetrics(redissonClient);

        // When
        binder.bindTo(meterRegistry);

        // Then
        assertThat(meterRegistry.find("redis.server.used_memory").gauge()).isNotNull();
    }

    @Test
    void redisServerHealthIndicator_ShouldReturnUpWithDetails() throws Exception {
        // Given
        RedisSingle redisSingle = mock(RedisSingle.class);
        RedisMaster redisNode = mock(RedisMaster.class);
        
        when(redissonClient.getRedisNodes(any())).thenReturn(redisSingle);
        when(redisSingle.getInstance()).thenReturn(redisNode);
        
        Map<String, String> info = new HashMap<>();
        info.put("used_memory", "1048576");
        info.put("used_memory_human", "1MB");
        info.put("maxmemory_human", "100MB");
        info.put("mem_fragmentation_ratio", "1.5");
        
        when(redisNode.info(any())).thenReturn(info);
        
        HealthIndicator healthIndicator = configuration.redisServerHealthIndicator(redissonClient);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails())
            .containsEntry("used_memory", "1048576")
            .containsEntry("used_memory_human", "1MB")
            .containsEntry("max_memory_human", "100MB");
    }

    @Test
    void redisServerHealthIndicator_WhenInfoFailsButPingWorks_ShouldReturnUp() throws Exception {
        // Given
        RedisSingle redisSingle = mock(RedisSingle.class);
        RedisMaster redisNode = mock(RedisMaster.class);
        
        when(redissonClient.getRedisNodes(any())).thenReturn(redisSingle);
        when(redisSingle.getInstance()).thenReturn(redisNode);
        
        when(redisNode.info(any())).thenThrow(new RuntimeException("Info failed"));
        when(redisNode.ping()).thenReturn(true);
        
        HealthIndicator healthIndicator = configuration.redisServerHealthIndicator(redissonClient);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("message", "Redis is up, but couldn't fetch detailed info");
    }
}
