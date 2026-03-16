package com.daangcool.stack.config;

import org.hibernate.cache.jcache.ConfigSettings;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

import static com.daangcool.stack.service.GlobalSettingsService.SETTING_CACHE;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_BY_ID;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_COUNT_BY_USER;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_COUNT_TOTAL;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_NOTICE_LIST;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_PAGE;
import static com.daangcool.stack.service.board.BoardService.CACHE_BOARD_SEARCH;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_BY_BOARD;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_BY_ID;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_COUNT_BY_BOARD;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_COUNT_BY_USER;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_SEARCH;
import static com.daangcool.stack.service.board.CommentService.CACHE_COMMENT_STATS;
import static com.daangcool.stack.service.board.TagService.CACHE_TAG_ALL;
import static com.daangcool.stack.service.board.TagService.CACHE_TAG_BY_ID;
import static com.daangcool.stack.service.board.TagService.CACHE_TAG_POPULAR;
import static com.daangcool.stack.service.board.TagService.CACHE_TAG_PREFIX;
import static com.daangcool.stack.service.board.UploadService.CACHE_UPLOAD_ALL;
import static com.daangcool.stack.service.board.UploadService.CACHE_UPLOAD_BY_BOARD;
import static com.daangcool.stack.service.board.UploadService.CACHE_UPLOAD_BY_ID;
import static com.daangcool.stack.service.board.UploadService.CACHE_UPLOAD_STATS;
import static com.daangcool.stack.service.common.CommonCodeService.COMMON_DETAIL_CACHE;
import static com.daangcool.stack.service.common.CommonCodeService.COMMON_DETAIL_LIST_BY_GROUP_CACHE;
import static com.daangcool.stack.service.common.CommonCodeService.COMMON_GROUP_CACHE;
import static com.daangcool.stack.service.common.CommonCodeService.COMMON_GROUP_LIST_CACHE;


/**
 * CacheConfiguration
 * ------------------------------------------------------------------
 * Redis/Redisson 기반 JCache 설정
 *
 * 변경 이력:
 *  - 2026-03-14: H-1 해결 — jcacheConfiguration 이 redissonClient 빈 재사용
 *  - 2026-03-14: M-5 개선 — buildTTLConfig() TTL 세분화 적용
 *                · 정적/준정적 데이터 (Tag, CommonCode, Settings): 24시간 TTL
 *                · 동적 데이터 (Board, Comment): 기본 TTL (1시간)
 *  - 2026-03-14: BUG 수정 — Board.boardTags 중복 등록 제거
 *  - 2026-03-15: W-4 해결 — setPassword() deprecated API 제거
 *                패스워드를 URL 에 포함한 채로 setAddress() 에 전달합니다.
 *                (redis://:password@host:port 형식)
 *                @SuppressWarnings("deprecation") 어노테이션 제거 완료.
 * ------------------------------------------------------------------
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    // -----------------------------------------------------------------
    // RedissonClient Bean (단일 인스턴스)
    // jcacheConfiguration 이 이 빈을 주입받아 재사용합니다.
    // -----------------------------------------------------------------
    @Bean
    public RedissonClient redissonClient(JHipsterProperties jHipsterProperties) {
        Config config = getRedissonConfig(jHipsterProperties);
        return Redisson.create(config);
    }

    // -----------------------------------------------------------------
    // JCache 기본 설정 Bean (jhipster 기본 TTL — 1시간)
    // -----------------------------------------------------------------
    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(
            RedissonClient redissonClient, JHipsterProperties props) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS,
                props.getCache().getRedis().getExpiration())));
        return RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);
    }

    // -----------------------------------------------------------------
    // JCache 장기 TTL Bean (정적/준정적 데이터 — 24시간)
    // Tag, CommonCode, Settings 등 변경 빈도가 낮은 데이터에 사용합니다.
    // -----------------------------------------------------------------
    @Bean
    public javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration(
            RedissonClient redissonClient) {
        return buildTTLConfig(redissonClient, 24, TimeUnit.HOURS);
    }

    // -----------------------------------------------------------------
    // Hibernate L2 Cache 연동
    // -----------------------------------------------------------------
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cm) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cm);
    }

    // -----------------------------------------------------------------
    // Cache Region 등록
    // -----------------------------------------------------------------
    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(
            javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration,
            javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration) {
        return cm -> {

            // ── 사용자 인증 캐시 (기본 TTL: 1시간) ─────────────────────
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName() + ".authorities", jcacheConfiguration);

            // ── Settings (장기 TTL: 24시간) ──────────────────────────────
            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(), longTtlCacheConfiguration);
            createCache(cm, SETTING_CACHE, longTtlCacheConfiguration);

            // ── CommonCode (장기 TTL: 24시간) ────────────────────────────
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), longTtlCacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), longTtlCacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", longTtlCacheConfiguration);
            createCache(cm, COMMON_GROUP_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_GROUP_LIST_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_DETAIL_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_DETAIL_LIST_BY_GROUP_CACHE, longTtlCacheConfiguration);

            // ── Tag (장기 TTL: 24시간) ───────────────────────────────────
            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_BY_ID, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_ALL, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_PREFIX, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_POPULAR, jcacheConfiguration); // 인기 태그는 1시간 TTL

            // ── Upload (기본 TTL: 1시간) ──────────────────────────────────
            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_ALL, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_STATS, jcacheConfiguration);

            // ── Comment (기본 TTL: 1시간) ─────────────────────────────────
            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_SEARCH, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_USER, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_STATS, jcacheConfiguration);

            // ── Board (기본 TTL: 1시간) ────────────────────────────────────
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".comments", jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".attachments", jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".boardTags", jcacheConfiguration);
            createCache(cm, CACHE_BOARD_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_BOARD_PAGE, jcacheConfiguration);
            createCache(cm, CACHE_BOARD_SEARCH, jcacheConfiguration);
            createCache(cm, CACHE_BOARD_NOTICE_LIST, jcacheConfiguration);
            createCache(cm, CACHE_BOARD_COUNT_TOTAL, jcacheConfiguration);
            createCache(cm, CACHE_BOARD_COUNT_BY_USER, jcacheConfiguration);

            // ── BoardTag (기본 TTL: 1시간) ────────────────────────────────
            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), jcacheConfiguration);

            // ── EmailOtpLog (기본 TTL: 1시간) ─────────────────────────────
            createCache(cm, com.daangcool.stack.domain.EmailOtpLog.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.EmailOtpLog.class.getName() + ".user", jcacheConfiguration);

            // ── Hibernate Query Cache ──────────────────────────────────────
            createCache(cm, "default-update-timestamps-region", jcacheConfiguration);
            createCache(cm, "default-query-results-region", jcacheConfiguration);

            // jhipster-needle-redis-add-entry
        };
    }

    // -----------------------------------------------------------------
    // 내부 헬퍼
    // -----------------------------------------------------------------

    private void createCache(
        javax.cache.CacheManager cm,
        String cacheName,
        javax.cache.configuration.Configuration<Object, Object> config
    ) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, config);
        }
    }

    /**
     * 개별 TTL을 지정한 캐시 설정을 생성합니다.
     *
     * @param redissonClient Redisson 클라이언트 (단일 인스턴스 재사용)
     * @param duration       TTL 시간값
     * @param unit           TTL 단위 (예: TimeUnit.HOURS)
     */
    private javax.cache.configuration.Configuration<Object, Object> buildTTLConfig(
            RedissonClient redissonClient, long duration, TimeUnit unit) {
        MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
        config.setStatisticsEnabled(true);
        config.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(unit, duration))
        );
        return RedissonConfiguration.fromInstance(redissonClient, config);
    }

    /**
     * Redisson 연결 설정을 구성합니다.
     *
     * <p>패스워드 처리 방식 (W-4 개선 — setPassword() deprecated API 제거):
     * <ul>
     *   <li>패스워드가 없는 경우: {@code redis://host:port} 그대로 사용</li>
     *   <li>패스워드가 있는 경우: {@code redis://:password@host:port} 형식으로
     *       프로퍼티에 기입하면 setAddress() 가 URL 을 직접 파싱합니다.</li>
     * </ul>
     *
     * <p>설정 예시 (application-prod.yml):
     * <pre>
     * jhipster:
     *   cache:
     *     redis:
     *       server: redis://:mypassword@localhost:6379
     * </pre>
     *
     * <p>클러스터 모드 예시:
     * <pre>
     * jhipster:
     *   cache:
     *     redis:
     *       server: redis://:mypassword@node1:6379,redis://:mypassword@node2:6379
     *       cluster: true
     * </pre>
     */
    private Config getRedissonConfig(JHipsterProperties jHipsterProperties) {
        Config config = new Config();
        // Hibernate lazy initialization 호환: https://github.com/jhipster/generator-jhipster/issues/22889
        config.setCodec(new org.redisson.codec.SerializationCodec());

        if (jHipsterProperties.getCache().getRedis().isCluster()) {
            config
                .useClusterServers()
                .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                // 패스워드가 포함된 경우 redis://:password@host:port 형식으로 기입하면
                // Redisson 이 URL 을 직접 파싱하여 인증합니다. setPassword() 호출 불필요.
                .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());
        } else {
            config
                .useSingleServer()
                .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                // 패스워드가 포함된 경우 redis://:password@host:port 형식으로 기입하면
                // Redisson 이 URL 을 직접 파싱하여 인증합니다. setPassword() 호출 불필요.
                .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);
        }
        return config;
    }
}
