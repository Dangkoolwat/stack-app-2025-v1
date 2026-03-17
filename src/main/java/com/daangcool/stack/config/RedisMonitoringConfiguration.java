package com.daangcool.stack.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisNode;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.api.redisnode.RedisSingle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis (Redisson) 모니터링 및 상태 지표 설정.
 * (C-4/W-1 연계 보안 및 가용성 모니터링 강화)
 */
@Configuration
public class RedisMonitoringConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisMonitoringConfiguration.class);

    private final AtomicLong usedMemoryGauge = new AtomicLong(0);

    /**
     * Redis 서버의 주요 상태를 Micrometer Gauge 로 등록합니다.
     * (Redisson OSS 버전에서 micrometer 브릿지 없이 수동 등록)
     */
    @Bean
    public MeterBinder redisMetrics(RedissonClient redissonClient) {
        return registry -> {
            log.info("[MONITORING] Registering Manual Redis metrics to MeterRegistry");
            
            Gauge.builder("redis.server.used_memory", usedMemoryGauge, AtomicLong::get)
                 .description("Current used memory of Redis server in bytes")
                 .baseUnit("bytes")
                 .register(registry);
            
            // 주기적으로 메모리 정보를 갱신하는 별도 스케줄러가 필요할 수 있으나, 
            // 여기서는 HealthCheck 시점에 갱신된 값을 사용하거나 직접 호출하는 방식을 취합니다.
        };
    }

    /**
     * Redis 서버의 세부 정보(메모리 사용량 등)를 Actuator Health 에 포함합니다.
     */
    @Bean
    public HealthIndicator redisServerHealthIndicator(RedissonClient redissonClient) {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) throws Exception {
                try {
                    // INFO command 를 통해 메모리 정보 추출
                    RedisSingle nodes = redissonClient.getRedisNodes(RedisNodes.SINGLE);
                    RedisNode node = nodes.getInstance();
                    
                    if (node == null) {
                        builder.unknown().withDetail("message", "No single Redis node found");
                        return;
                    }

                    Map<String, String> info = node.info(RedisNode.InfoSection.MEMORY);
                    
                    String usedMemoryStr = info.get("used_memory");
                    if (usedMemoryStr != null) {
                        try {
                            usedMemoryGauge.set(Long.parseLong(usedMemoryStr));
                        } catch (NumberFormatException ignored) {}
                    }

                    builder.up()
                           .withDetail("used_memory_human", info.getOrDefault("used_memory_human", "unknown"))
                           .withDetail("max_memory_human", info.getOrDefault("maxmemory_human", "unknown"))
                           .withDetail("fragmentation_ratio", info.getOrDefault("mem_fragmentation_ratio", "unknown"))
                           .withDetail("used_memory", usedMemoryStr);
                           
                } catch (Exception e) {
                    log.warn("[MONITORING] Failed to fetch Redis memory info for health check: {}", e.getMessage());
                    // 기본 연결 확인 (ping)
                    try {
                        RedisSingle nodes = redissonClient.getRedisNodes(RedisNodes.SINGLE);
                        if (nodes != null && nodes.getInstance() != null && nodes.getInstance().ping()) {
                            builder.up().withDetail("message", "Redis is up, but couldn't fetch detailed info");
                        } else {
                            builder.down().withDetail("error", "Redis is unreachable");
                        }
                    } catch (Exception pingEx) {
                        builder.down(pingEx).withDetail("error", "Redis is unreachable: " + pingEx.getMessage());
                    }
                }
            }
        };
    }
}
