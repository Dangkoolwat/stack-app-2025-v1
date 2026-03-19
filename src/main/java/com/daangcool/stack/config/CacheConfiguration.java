package com.daangcool.stack.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public RedissonClient redissonJsonClient(JHipsterProperties jHipsterProperties) {
        // Specialized client: JSON codec for Spring @Cacheable readability and entity support
        Config config = getRedissonConfig(jHipsterProperties);
        config.setCodec(new org.redisson.codec.JsonJacksonCodec(createSpringCacheObjectMapper()));
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
        // Spring Caches: Use JSON client
        javax.cache.configuration.Configuration<Object, Object> springConfig = buildTTLConfig(
            redissonJsonClient,
            jHipsterProperties.getCache().getRedis().getExpiration(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> springLongTtlConfig = buildTTLConfig(redissonJsonClient, 86400, TimeUnit.SECONDS);

        // Hibernate L2 Cache regions: Use Binary client (via jcacheConfiguration / longTtlCacheConfiguration)
        javax.cache.configuration.Configuration<Object, Object> binaryConfig = buildTTLConfig(
            redissonClient,
            jHipsterProperties.getCache().getRedis().getExpiration(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> binaryLongConfig = buildTTLConfig(redissonClient, 86400, TimeUnit.SECONDS);

        return cm -> {
            // createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_LOGIN_CACHE, binaryConfig); // Removed to break loop
            // createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_EMAIL_CACHE, binaryConfig); // Removed to break loop

            createCache(cm, com.daangcool.stack.domain.User.class.getName(), binaryConfig);
            createCache(cm, com.daangcool.stack.domain.Authority.class.getName(), binaryConfig);
            createCache(cm, com.daangcool.stack.domain.User.class.getName() + ".authorities", binaryConfig);

            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(), binaryLongConfig);
            createCache(cm, SETTING_CACHE, springLongTtlConfig);

            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), binaryLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), binaryLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", binaryLongConfig);
            createCache(cm, COMMON_GROUP_CACHE, springLongTtlConfig);
            createCache(cm, COMMON_GROUP_LIST_CACHE, springLongTtlConfig);
            createCache(cm, COMMON_DETAIL_CACHE, springLongTtlConfig);
            createCache(cm, COMMON_DETAIL_LIST_BY_GROUP_CACHE, springLongTtlConfig);

            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), binaryLongConfig);
            createCache(cm, CACHE_TAG_BY_ID, springLongTtlConfig);
            createCache(cm, CACHE_TAG_ALL, springLongTtlConfig);
            createCache(cm, CACHE_TAG_PREFIX, springLongTtlConfig);
            createCache(cm, CACHE_TAG_POPULAR, springConfig);

            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), binaryConfig);
            createCache(cm, CACHE_UPLOAD_BY_ID, springConfig);
            createCache(cm, CACHE_UPLOAD_BY_BOARD, springConfig);
            createCache(cm, CACHE_UPLOAD_ALL, springConfig);
            createCache(cm, CACHE_UPLOAD_STATS, springConfig);

            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), binaryConfig);
            createCache(cm, CACHE_COMMENT_BY_ID, springConfig);
            createCache(cm, CACHE_COMMENT_BY_BOARD, springConfig);
            createCache(cm, CACHE_COMMENT_SEARCH, springConfig);
            createCache(cm, CACHE_COMMENT_COUNT_BY_BOARD, springConfig);
            createCache(cm, CACHE_COMMENT_COUNT_BY_USER, springConfig);
            createCache(cm, CACHE_COMMENT_STATS, springConfig);

            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName(), binaryConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".comments", binaryConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".attachments", binaryConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".boardTags", binaryConfig);
            createCache(cm, CACHE_BOARD_BY_ID, springConfig);
            createCache(cm, CACHE_BOARD_PAGE, springConfig);
            createCache(cm, CACHE_BOARD_SEARCH, springConfig);
            createCache(cm, CACHE_BOARD_NOTICE_LIST, springConfig);
            createCache(cm, CACHE_BOARD_COUNT_TOTAL, springConfig);
            createCache(cm, CACHE_BOARD_COUNT_BY_USER, springConfig);

            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), binaryConfig);

            createCache(cm, "default-update-timestamps-region", binaryConfig);
            createCache(cm, "default-query-results-region", binaryConfig);
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

    private ObjectMapper createSpringCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.setAnnotationIntrospector(
            new com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector() {
                @Override
                public boolean hasIgnoreMarker(com.fasterxml.jackson.databind.introspect.AnnotatedMember m) {
                    return false;
                }
            }
        );
        mapper.addMixIn(org.hibernate.collection.spi.PersistentSet.class, HibernateSetMixIn.class);
        mapper.addMixIn(org.hibernate.collection.spi.PersistentBag.class, HibernateBagMixIn.class);
        return mapper;
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

    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NONE)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = java.util.HashSet.class)
    abstract static class HibernateSetMixIn {}

    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NONE)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = java.util.ArrayList.class)
    abstract static class HibernateBagMixIn {}
}
