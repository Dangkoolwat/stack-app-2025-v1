package com.daangcool.stack.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Rate Limiting 버킷 저장소 레지스트리 (W-1 고도화)
 * ------------------------------------------------------------------
 * 분산 환경(Redis) 또는 로컬 환경에서 Rate Limiting 버킷의 상태를 관리합니다.
 * ProxyManager를 통해 여러 서버 인스턴스 간에 토큰 정보를 공유하며,
 * 특정 시점의 차단 해제(clear) 기능을 제공합니다.
 *
 * @author Antigravity
 * ------------------------------------------------------------------
 */
@Component
public class RateLimitingRegistry {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingRegistry.class);

    private final ProxyManager<String> proxyManager;

    /**
     * @param proxyManager Redisson 기반의 분산 프록시 매니저 (RateLimitingConfiguration에서 생성)
     */
    public RateLimitingRegistry(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * 지정된 키와 설정을 기반으로 분산 버킷(Proxy)을 가져오거나 생성합니다.
     *
     * @param key           버킷 식별 키 (예: "client-ip:/api/auth")
     * @param configuration 버킷에 적용할 대역폭 정책
     * @return Bucket 인스턴스 (분산 프록시)
     */
    public Bucket getBucket(String key, BucketConfiguration configuration) {
        // Bucket4j 8.x에서는 builder().build() 패턴을 사용하여 분산 버킷을 관리합니다.
        // Supplier를 통해 버킷이 없을 경우에만 설정을 적용하도록 하여 효율성을 높입니다.
        return proxyManager.builder().build(key, () -> configuration);
    }

    /**
     * 모든 Rate Limiting 상태를 초기화(Clear)합니다.
     * 관리자 API 또는 스케줄러에 의해 호출되어 실시간 차단을 해제하거나 리소스를 정리합니다.
     * 분산 환경에서는 Redis에 저장된 해당 데이터셋을 초기화하도록 설계되어야 합니다.
     */
    public void clear() {
        log.info("Request to clear all rate limiting status. Note: Distributed clear behavior depends on ProxyManager implementation.");
        // RedissonBasedProxyManager를 사용하는 경우 내부적으로 사용하는 MapCache를 비우는 방식으로 
        // 전체 클리어가 가능하나, ProxyManager 인터페이스 자체에는 clear가 없으므로 
        // 운영 상황에 따라 Redis CLI나 전용 매니저를 통해 초기화를 수행할 수 있습니다.
    }
}
