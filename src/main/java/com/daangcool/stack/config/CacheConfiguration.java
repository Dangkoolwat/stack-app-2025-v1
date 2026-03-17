package com.daangcool.stack.config;

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
 *  - 2026-03-17: NM-5 해결 — Redisson 코덱을 JsonJacksonCodec으로 전환
 *  - 2026-03-17: IDE 심볼 오류 조치 — javax.cache:cache-api 의존성 명시 및 패키지 복구
 * ------------------------------------------------------------------
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(JHipsterProperties jHipsterProperties) {
        Config config = getRedissonConfig(jHipsterProperties);
        return Redisson.create(config);
    }

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

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration(
            RedissonClient redissonClient) {
        return buildTTLConfig(redissonClient, 24, TimeUnit.HOURS);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cm) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cm);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(
            javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration,
            javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration) {
        return cm -> {
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName() + ".authorities", jcacheConfiguration);

            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(), longTtlCacheConfiguration);
            createCache(cm, SETTING_CACHE, longTtlCacheConfiguration);

            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), longTtlCacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), longTtlCacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", longTtlCacheConfiguration);
            createCache(cm, COMMON_GROUP_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_GROUP_LIST_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_DETAIL_CACHE, longTtlCacheConfiguration);
            createCache(cm, COMMON_DETAIL_LIST_BY_GROUP_CACHE, longTtlCacheConfiguration);

            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_BY_ID, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_ALL, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_PREFIX, longTtlCacheConfiguration);
            createCache(cm, CACHE_TAG_POPULAR, jcacheConfiguration);

            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_ALL, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_STATS, jcacheConfiguration);

            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_SEARCH, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_USER, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_STATS, jcacheConfiguration);

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

            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), jcacheConfiguration);

            createCache(cm, "default-update-timestamps-region", jcacheConfiguration);
            createCache(cm, "default-query-results-region", jcacheConfiguration);
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
            RedissonClient redissonClient, long duration, TimeUnit unit) {
        MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
        config.setStatisticsEnabled(true);
        config.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(unit, duration))
        );
        return RedissonConfiguration.fromInstance(redissonClient, config);
    }

    private Config getRedissonConfig(JHipsterProperties jHipsterProperties) {
        Config config = new Config();
        config.setCodec(new org.redisson.codec.JsonJacksonCodec());

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
