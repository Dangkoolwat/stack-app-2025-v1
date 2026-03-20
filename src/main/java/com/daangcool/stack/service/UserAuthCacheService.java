package com.daangcool.stack.service;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.service.dto.UserAuthCacheDto;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 인증용 사용자 정보 Redis 2차 캐시 서비스
 * ------------------------------------------------------------------
 * - 대상 클라이언트: redissonJsonClient (Jackson 3 / JsonJackson3Codec)
 *   → CacheConfiguration 에 이미 등록된 빈을 재사용 (추가 인프라 불필요)
 * - 키 패턴: "auth:user:{login}"
 *   → 기존 JCache 영역("board:", "otp:", "rl:") 과 prefix 가 달라 충돌 없음
 * - TTL: application.auth-cache.ttl-minutes (기본 5분)
 *   → Access Token 유효기간보다 짧게 유지
 * - Fallback: Redis 장애 시 예외를 삼키고 Optional.empty() 반환
 *   → DB 조회로 자동 폴백, 서비스 중단 없음
 *
 * 무효화(evict) 호출 지점:
 *   UserService.changePassword()
 *   UserService.updateUser()       (관리자 권한 수정 포함)
 *   UserService.activateRegistration()
 *   UserService.deleteUser()
 * ------------------------------------------------------------------
 */
@Service
public class UserAuthCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthCacheService.class);

    /** Redis 키 접두사 — 기존 캐시 영역과 충돌 방지 */
    static final String KEY_PREFIX = "auth:user:";

    /** TTL (분) — ApplicationProperties 에서 주입 */
    private final long ttlMinutes;

    /** JsonJackson3Codec 이 적용된 Redisson 클라이언트 */
    private final RedissonClient redissonJsonClient;

    public UserAuthCacheService(
        @Qualifier("redissonJsonClient") RedissonClient redissonJsonClient,
        ApplicationProperties applicationProperties
    ) {
        this.redissonJsonClient = redissonJsonClient;
        this.ttlMinutes = applicationProperties.getAuthCache().getTtlMinutes();
    }

    // ------------------------------------------------------------------
    // 조회
    // ------------------------------------------------------------------

    /**
     * Redis 에서 인증 캐시를 조회합니다.
     *
     * @param login 사용자 로그인 (login or email 둘 다 키로 사용 가능)
     * @return 캐시 히트 시 Optional<UserAuthCacheDto>, 미스/장애 시 Optional.empty()
     */
    public Optional<UserAuthCacheDto> get(String login) {
        try {
            RBucket<UserAuthCacheDto> bucket = redissonJsonClient.getBucket(KEY_PREFIX + login);
            UserAuthCacheDto value = bucket.get();
            if (value != null) {
                log.debug("[AuthCache] HIT: {}", login);
            } else {
                log.debug("[AuthCache] MISS: {}", login);
            }
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.warn("[AuthCache] Redis 조회 실패 → DB fallback 진행: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // ------------------------------------------------------------------
    // 저장
    // ------------------------------------------------------------------

    /**
     * 인증 정보를 Redis 에 저장합니다.
     * 영속성 컨텍스트(트랜잭션) 안에서 호출해야 LazyLoad 가 정상 동작합니다.
     *
     * @param login  키로 사용할 login 문자열
     * @param dto    캐시할 UserAuthCacheDto
     */
    public void put(String login, UserAuthCacheDto dto) {
        try {
            RBucket<UserAuthCacheDto> bucket = redissonJsonClient.getBucket(KEY_PREFIX + login);
            bucket.set(dto, ttlMinutes, TimeUnit.MINUTES);
            log.debug("[AuthCache] STORE: {} (TTL {}분)", login, ttlMinutes);
        } catch (Exception e) {
            log.warn("[AuthCache] Redis 저장 실패 (무시하고 계속): {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // 무효화
    // ------------------------------------------------------------------

    /**
     * 사용자 인증 캐시를 즉시 무효화합니다.
     * 비밀번호 변경 / 권한 수정 / 계정 정지 / 탈퇴 시 반드시 호출합니다.
     *
     * @param login 무효화할 사용자 login
     */
    public void evict(String login) {
        if (login == null || login.isBlank()) return;
        try {
            boolean deleted = redissonJsonClient.getBucket(KEY_PREFIX + login).delete();
            log.info("[AuthCache] EVICT: {} (삭제됨={})", login, deleted);
        } catch (Exception e) {
            log.warn("[AuthCache] Redis 무효화 실패 (무시하고 계속): {}", e.getMessage());
        }
    }
}
