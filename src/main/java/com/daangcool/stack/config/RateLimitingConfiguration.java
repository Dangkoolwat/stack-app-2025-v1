package com.daangcool.stack.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Rate Limiting 분산 환경 설정을 위한 구성 클래스 (W-1)
 * ------------------------------------------------------------------
 * Redis(Redisson)를 기반으로 여러 애플리케이션 인스턴스 간에 
 * Rate Limiting 상태를 공유할 수 있도록 ProxyManager를 설정합니다.
 * ------------------------------------------------------------------
 */
@Configuration
public class RateLimitingConfiguration {

    private final RedissonClient defaultRedissonClient;

    /**
     * @param defaultRedissonClient 기본 캐시용 RedissonClient (fallback용)
     */
    public RateLimitingConfiguration(RedissonClient defaultRedissonClient) {
        this.defaultRedissonClient = defaultRedissonClient;
    }

    /**
     * Rate Limiting 버킷 관리를 위한 ProxyManager 빈을 생성합니다.
     * 기본 캐시용 RedissonClient를 재사용합니다. (불필요한 추가 Redis 연결 생성 방지)
     *
     * @return Redisson 기반의 ProxyManager
     */
    @Bean
    public ProxyManager<String> rateLimitProxyManager() {
        RedissonClient client = defaultRedissonClient;

        // [Phase 3] 타입 호환성(Incompatible types) 문제를 해결하기 위해 
        // 리플렉션을 사용하여 RedissonClient에서 CommandAsyncExecutor를 추출합니다.
        CommandAsyncExecutor commandExecutor;
        try {
            Method getCommandExecutor = client.getClass().getMethod("getCommandExecutor");
            commandExecutor = (CommandAsyncExecutor) getCommandExecutor.invoke(client);
        } catch (Exception e) {
            // 리플렉션 실패 시 직접 캐스팅 시도 (일부 환경에서 발생할 수 있는 캐스팅 오류 대비)
            try {
                commandExecutor = ((Redisson) client).getCommandExecutor();
            } catch (ClassCastException cce) {
                throw new RuntimeException("Could not obtain CommandAsyncExecutor from RedissonClient. " +
                    "Ensure you are using a standard RedissonClient implementation.", e);
            }
        }

        // [Phase 3] Deprecated된 withExpirationStrategy 대신 ClientSideConfig를 사용합니다.
        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
            .withExpirationAfterWriteStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1)));

        return RedissonBasedProxyManager.builderFor(commandExecutor)
            .withClientSideConfig(clientSideConfig)
            .build();
    }
}
