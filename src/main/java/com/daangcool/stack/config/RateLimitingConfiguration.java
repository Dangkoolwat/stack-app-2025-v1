package com.daangcool.stack.config;

import org.springframework.context.annotation.Configuration;

/**
 * Rate Limiting 분산 환경 설정을 위한 구성 클래스 (W-1)
 * ------------------------------------------------------------------
 * Redis(Redisson)를 기반으로 여러 애플리케이션 인스턴스 간에 
 * Rate Limiting 상태를 공유할 수 있도록 ProxyManager를 설정합니다.
 * ------------------------------------------------------------------
 */
@Configuration
public class RateLimitingConfiguration {
    // [Phase 4] Redisson Native 전환 완료. 
    // ProxyManager(Bucket4j)가 더 이상 필요하지 않으므로 빈 설정을 제거합니다.
    // 모든 처리는 RateLimitingRegistry에서 RedissonClient를 직접 사용하여 이루어집니다.
}
