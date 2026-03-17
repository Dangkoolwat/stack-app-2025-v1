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
        
        /**
         * NH-1 보안/성능 개선:
         * trySetRate는 설정이 존재하지 않을 때만 Redis에 저장합니다 (부하 최소화).
         * 운영 환경에서 tokens/duration 수치를 변경했을 경우, Admin API의 /clear 
         * 또는 매일 새벽 스케줄러에 의해 기존 데이터가 삭제된 후 새 설정이 반영됩니다.
         * 매 요청마다 setRate를 호출하지 않는 이유는 런타임 오버헤드와 
         * 기존 토큰 잔량의 초기화(Reset) 부작용을 방지하기 위함입니다.
         */
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
