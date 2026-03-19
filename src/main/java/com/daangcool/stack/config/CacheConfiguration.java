package com.daangcool.stack.config;

import tools.jackson.databind.ObjectMapper;
import org.hibernate.cache.jcache.ConfigSettings;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
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
 *  - 2026-03-17: NM-5 해결 — Redisson 코덱을 JsonJacksonCodec으로 전환
 *  - 2026-03-17: IDE 심볼 오류 조치 — javax.cache:cache-api 의존성 명시 및 패키지 복구
 *  - 2026-03-20: C-1 리팩토링 — 인증 캐시(User, Authority) 제거 및 캐시 영역 서비스 단위 그룹화/TTL 재설계
 * ------------------------------------------------------------------
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean(destroyMethod = "shutdown")
    @org.springframework.context.annotation.Primary
    public RedissonClient redissonClient(JHipsterProperties jHipsterProperties) {
        // Default client: Binary codec for internal Hibernate L2 cache stability
        return Redisson.create(getRedissonConfig(jHipsterProperties));
    }

    @Bean(name = "redissonJsonClient", destroyMethod = "shutdown")
    public RedissonClient redissonJsonClient(JHipsterProperties jHipsterProperties, ObjectMapper objectMapper) {
        // [REFAC] NM-5: ObjectMapper를 직접 생성하지 않고 Spring 빈 주입 방식으로 변경 (Jackson 3 호환)
        Config config = getRedissonConfig(jHipsterProperties);
        // [REFAC] JsonJacksonCodec 대신 Jackson 3를 지원하는 JsonJackson3Codec 사용
        config.setCodec(new org.redisson.codec.JsonJackson3Codec(objectMapper));
        return Redisson.create(config);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(
        RedissonClient redissonClient,
        JHipsterProperties props
    ) {
        return buildTTLConfig(redissonClient, props.getCache().getRedis().getExpiration(), TimeUnit.SECONDS);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration(RedissonClient redissonClient) {
        return buildTTLConfig(redissonClient, 24, TimeUnit.HOURS);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cm) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cm);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(
        @org.springframework.beans.factory.annotation.Qualifier("redissonJsonClient") RedissonClient redissonJsonClient,
        RedissonClient redissonClient, // Primary (Binary)
        JHipsterProperties jHipsterProperties
    ) {
        // Spring Caches: Use JSON client (Default TTL 1h / Long TTL 24h)
        javax.cache.configuration.Configuration<Object, Object> applicationDefaultConfig = buildTTLConfig(
            redissonJsonClient,
            jHipsterProperties.getCache().getRedis().getExpiration(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> applicationLongConfig = buildTTLConfig(redissonJsonClient, 86400, TimeUnit.SECONDS);

        // Hibernate L2 Cache regions: Use Binary client (Default TTL 1h / Long TTL 24h)
        javax.cache.configuration.Configuration<Object, Object> hibernateDefaultConfig = buildTTLConfig(
            redissonClient,
            jHipsterProperties.getCache().getRedis().getExpiration(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> hibernateLongConfig = buildTTLConfig(redissonClient, 86400, TimeUnit.SECONDS);

        return cm -> {
            // A. 전역 설정 및 공통 코드 (Long TTL)
            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(), hibernateLongConfig);
            createCache(cm, SETTING_CACHE, applicationLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", hibernateLongConfig);
            createCache(cm, COMMON_GROUP_CACHE, applicationLongConfig);
            createCache(cm, COMMON_GROUP_LIST_CACHE, applicationLongConfig);
            createCache(cm, COMMON_DETAIL_CACHE, applicationLongConfig);
            createCache(cm, COMMON_DETAIL_LIST_BY_GROUP_CACHE, applicationLongConfig);

            // B. 게시판 (Board) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName(), hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".comments", hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".attachments", hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".boardTags", hibernateDefaultConfig);
            createCache(cm, CACHE_BOARD_BY_ID, applicationDefaultConfig);
            createCache(cm, CACHE_BOARD_PAGE, applicationDefaultConfig);
            createCache(cm, CACHE_BOARD_SEARCH, applicationDefaultConfig);
            createCache(cm, CACHE_BOARD_NOTICE_LIST, applicationDefaultConfig);
            createCache(cm, CACHE_BOARD_COUNT_TOTAL, applicationDefaultConfig);
            createCache(cm, CACHE_BOARD_COUNT_BY_USER, applicationDefaultConfig);

            // C. 댓글 (Comment) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), hibernateDefaultConfig);
            createCache(cm, CACHE_COMMENT_BY_ID, applicationDefaultConfig);
            createCache(cm, CACHE_COMMENT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, CACHE_COMMENT_SEARCH, applicationDefaultConfig);
            createCache(cm, CACHE_COMMENT_COUNT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, CACHE_COMMENT_COUNT_BY_USER, applicationDefaultConfig);
            createCache(cm, CACHE_COMMENT_STATS, applicationDefaultConfig);

            // D. 태그 (Tag) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), hibernateDefaultConfig);
            createCache(cm, CACHE_TAG_BY_ID, applicationLongConfig);
            createCache(cm, CACHE_TAG_ALL, applicationLongConfig);
            createCache(cm, CACHE_TAG_PREFIX, applicationLongConfig);
            createCache(cm, CACHE_TAG_POPULAR, applicationDefaultConfig);

            // E. 업로드 (Upload) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), hibernateDefaultConfig);
            createCache(cm, CACHE_UPLOAD_BY_ID, applicationDefaultConfig);
            createCache(cm, CACHE_UPLOAD_BY_BOARD, applicationDefaultConfig);
            createCache(cm, CACHE_UPLOAD_ALL, applicationDefaultConfig);
            createCache(cm, CACHE_UPLOAD_STATS, applicationDefaultConfig);

            // F. 하이버네이트 기본 시스템 캐시
            createCache(cm, "default-update-timestamps-region", hibernateDefaultConfig);
            createCache(cm, "default-query-results-region", hibernateDefaultConfig);
        };
    }

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

    private javax.cache.configuration.Configuration<Object, Object> buildTTLConfig(
        RedissonClient redissonClient,
        long duration,
        TimeUnit unit
    ) {
        MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
        config.setStatisticsEnabled(true);
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(unit, duration)));
        return RedissonConfiguration.fromInstance(redissonClient, config);
    }



    private Config getRedissonConfig(JHipsterProperties jHipsterProperties) {
        Config config = new Config();
        if (jHipsterProperties.getCache().getRedis().isCluster()) {
            config
                .useClusterServers()
                .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());
        } else {
            config
                .useSingleServer()
                .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);
        }
        return config;
    }

}
