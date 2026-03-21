package com.daangcool.stack.security;

import com.daangcool.stack.config.ApplicationProperties;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * [UserAuthCacheService] 인증용 사용자 정보 Redis 2차 캐시 서비스
 *
 * 역할:
 * - 인증된 사용자 권한 정보를 Redis 에 캐시하여 DB 부하 감소
 * - UserService 의 상태 변경 시 해당 캐시 무효화(evict) 수행
 *
 * 에이전트 작업 가이드:
 * - 캐시 대상 데이터 구조 변경 시 UserAuthCacheDto 와 함께 수정
 * - Redisson 클라이언트(@Qualifier("redissonJsonClient")) 설정 확인 필요
 *
 * 주의사항:
 * - Redis 장애 시 DB로 자동 폴백(Fallback)되도록 로직이 설계됨
 * - TTL은 Access Token 보다 짧게 유지하는 것이 보안상 권장됨
 *
 * 변경 이력:
 * - 2026-03-21: [Move] service 패키지에서 security 패키지로 이동 (ArchUnit 대응)
 * - 2026-03-21: [Refactor] Deprecated 된 TimeUnit 대신 Duration API 적용
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
            bucket.set(dto, Duration.ofMinutes(ttlMinutes));
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
