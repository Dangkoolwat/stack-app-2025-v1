package com.daangcool.stack.service.otp;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Email OTP 캐시 서비스
 *
 * <p>
 * OTP 코드, 실패 횟수, 계정 잠금 상태를 Redis 기반으로 관리합니다.
 * <br> (RedissonClient 사용)
 * </p>
 *
 * <ul>
 *   <li>OTP 코드 저장 / 조회 / 삭제</li>
 *   <li>실패 횟수 관리</li>
 *   <li>잠금 상태 관리</li>
 * </ul>
 *
 * @author Steve
 * @since 2025-10-15
 */
@Service
public class EmailOtpCacheService {

    private static final Logger log = LoggerFactory.getLogger(EmailOtpCacheService.class);

    private static final String OTP_CODE_MAP = "otp:code";
    private static final String OTP_FAIL_MAP = "otp:fail";
    private static final String OTP_LOCK_MAP = "otp:lock";

    private final RedissonClient redissonClient;
    private final int maxFailureAttempts = 5;
    private final Duration otpTtl = Duration.ofMinutes(5);
    private final Duration lockTtl = Duration.ofMinutes(10);

    public EmailOtpCacheService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // ==========================================================
    // OTP 코드 관리
    // ==========================================================

    /**
     * OTP 코드를 캐시에 저장합니다.
     *
     * @param email 대상 이메일
     * @param code 6자리 OTP 코드
     */
    public void setOtpCode(String email, String code) {
        RMapCache<String, String> map = redissonClient.getMapCache(OTP_CODE_MAP);
        map.put(email, code, otpTtl.toMillis(), TimeUnit.MILLISECONDS);
        log.debug("[OTP Cache] {} → 코드 저장 (TTL {}분)", email, otpTtl.toMinutes());
    }

    /**
     * OTP 코드를 조회합니다.
     */
    public String getOtpCode(String email) {
        RMapCache<String, String> map = redissonClient.getMapCache(OTP_CODE_MAP);
        return map.get(email);
    }

    /**
     * OTP 코드를 삭제합니다.
     */
    public void deleteOtpCode(String email) {
        RMapCache<String, String> map = redissonClient.getMapCache(OTP_CODE_MAP);
        map.remove(email);
        log.debug("[OTP Cache] {} → 코드 삭제", email);
    }

    // ==========================================================
    // 실패 횟수 관리
    // ==========================================================

    /**
     * OTP 인증 실패 횟수를 1 증가시킵니다.
     */
    public void incrementFailureCount(String email) {
        RMapCache<String, Integer> map = redissonClient.getMapCache(OTP_FAIL_MAP);
        Integer count = map.getOrDefault(email, 0) + 1;
        map.put(email, count, lockTtl.toMillis(), TimeUnit.MILLISECONDS);
        log.debug("[OTP Cache] {} → 실패 횟수 {}", email, count);
    }

    /**
     * 실패 횟수를 조회합니다.
     */
    public int getFailureCount(String email) {
        RMapCache<String, Integer> map = redissonClient.getMapCache(OTP_FAIL_MAP);
        return map.getOrDefault(email, 0);
    }

    /**
     * 실패 횟수를 리셋합니다.
     */
    public void resetFailureCount(String email) {
        RMapCache<String, Integer> map = redissonClient.getMapCache(OTP_FAIL_MAP);
        map.remove(email);
        log.debug("[OTP Cache] {} → 실패 횟수 리셋", email);
    }

    /**
     * 최대 실패 허용 횟수 반환.
     */
    public int getMaxFailureAttempts() {
        return maxFailureAttempts;
    }

    // ==========================================================
    // 계정 잠금 관리
    // ==========================================================

    /**
     * 계정을 임시 잠금 처리합니다.
     */
    public void lockAccount(String email) {
        RMapCache<String, Boolean> map = redissonClient.getMapCache(OTP_LOCK_MAP);
        map.put(email, true, lockTtl.toMillis(), TimeUnit.MILLISECONDS);
        log.warn("[OTP Cache] {} → 계정 임시 잠금 ({}분)", email, lockTtl.toMinutes());
    }

    /**
     * 계정 잠금 여부 확인.
     */
    public boolean isLocked(String email) {
        RMapCache<String, Boolean> map = redissonClient.getMapCache(OTP_LOCK_MAP);
        return map.getOrDefault(email, false);
    }

    /**
     * 계정 잠금 해제.
     */
    public void unlockAccount(String email) {
        RMapCache<String, Boolean> map = redissonClient.getMapCache(OTP_LOCK_MAP);
        map.remove(email);
        log.info("[OTP Cache] {} → 계정 잠금 해제", email);
    }
}
