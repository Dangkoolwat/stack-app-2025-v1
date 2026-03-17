package com.daangcool.stack.security;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Rate Limiting 레지스트리 (W-1 고도화 / NH-1 보안 개선 반영)
 * ------------------------------------------------------------------
 * Redisson 네이티브 RRateLimiter를 사용하여 요청 제한 상태를 관리합니다.
 * Bucket4j 대신 Redisson 공식 API를 사용하여 리플렉션 없이 안전하게 
 * 분산 환경의 Rate Limiting을 수행합니다.
 *
 * @author Antigravity
 * ------------------------------------------------------------------
 */
@Component
public class RateLimitingRegistry {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingRegistry.class);

    private final RedissonClient redissonClient;

    /**
     * @param redissonClient 기본 캐시용 RedissonClient 재사용
     */
    public RateLimitingRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 지정된 키의 승인을 시도합니다.
     *
     * @param key             버킷 식별 키
     * @param tokens          허용되는 최대 토큰 수
     * @param durationMinutes 토큰 충전 주기 (분)
     * @return 승인 성공 시 true
     */
    public RateLimitResult tryConsume(String key, long tokens, long durationMinutes) {
        RRateLimiter limiter = redissonClient.getRateLimiter("rl:" + key);
        
        // 초기 설정 또는 설정 변경 시 업데이트 (trySetRate는 이미 설정된 경우 false 반환)
        limiter.trySetRate(RateType.OVERALL, tokens, Duration.ofMinutes(durationMinutes));
        
        boolean success = limiter.tryAcquire(1);
        long remaining = limiter.availablePermits();
        
        return new RateLimitResult(success, remaining);
    }

    /**
     * 모든 Rate Limiting 키를 초기화합니다.
     * rl:* 패턴의 키들을 일괄 삭제합니다.
     */
    public void clear() {
        log.info("Clearing all rate limit status in Redis (pattern: rl:*)");
        redissonClient.getKeys().deleteByPattern("rl:*");
    }

    /**
     * 결과 캡슐화 객체
     */
    public record RateLimitResult(boolean isConsumed, long remainingTokens) {}
}
