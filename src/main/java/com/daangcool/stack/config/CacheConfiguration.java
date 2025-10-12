package com.daangcool.stack.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;
import tech.jhipster.config.cache.PrefixedKeyGenerator;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static com.daangcool.stack.service.GlobalSettingsService.SETTING_CACHE;
import static com.daangcool.stack.service.board.BoardService.*;
import static com.daangcool.stack.service.board.CommentService.*;
import static com.daangcool.stack.service.board.TagService.*;
import static com.daangcool.stack.service.board.UploadService.*;
import static com.daangcool.stack.service.common.CommonCodeService.*;


@Configuration
@EnableCaching
public class CacheConfiguration {

    private GitProperties gitProperties;
    private BuildProperties buildProperties;

    @Bean
    public Config redissonConfig(JHipsterProperties jHipsterProperties) {
        URI redisUri = URI.create(jHipsterProperties.getCache().getRedis().getServer()[0]);
        Config config = new Config();
        // Fix Hibernate lazy initialization
        config.setCodec(new org.redisson.codec.SerializationCodec());

        if (jHipsterProperties.getCache().getRedis().isCluster()) {
            ClusterServersConfig clusterServersConfig = config
                .useClusterServers()
                .setMasterConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .addNodeAddress(jHipsterProperties.getCache().getRedis().getServer());

            if (redisUri.getUserInfo() != null) {
                clusterServersConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        } else {
            SingleServerConfig singleServerConfig = config
                .useSingleServer()
                .setConnectionPoolSize(jHipsterProperties.getCache().getRedis().getConnectionPoolSize())
                .setConnectionMinimumIdleSize(jHipsterProperties.getCache().getRedis().getConnectionMinimumIdleSize())
                .setSubscriptionConnectionPoolSize(jHipsterProperties.getCache().getRedis().getSubscriptionConnectionPoolSize())
                .setAddress(jHipsterProperties.getCache().getRedis().getServer()[0]);

            if (redisUri.getUserInfo() != null) {
                singleServerConfig.setPassword(redisUri.getUserInfo().substring(redisUri.getUserInfo().indexOf(':') + 1));
            }
        }
        return config;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Config redissonConfig) {
        return Redisson.create(redissonConfig);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(JHipsterProperties jHipsterProperties, RedissonClient redissonClient) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, jHipsterProperties.getCache().getRedis().getExpiration()))
        ); // 시스템 TTL 설정 부분 : 기본 24시간
        return RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(ObjectProvider<CacheManager> cmProvider) {
        return hibernateProperties -> hibernateProperties.put(org.hibernate.cache.jcache.ConfigSettings.CACHE_MANAGER, cmProvider.getObject());
    }

    @Bean
    public org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            // Hibernate Query Cache 기본 리전 추가


            // 기본 캐시
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_LOGIN_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.repository.UserRepository.USERS_BY_EMAIL_CACHE, jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.Authority.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.User.class.getName() + ".authorities", jcacheConfiguration);

            // Global Settings 기본 TTL 적용
            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(),jcacheConfiguration);
            createCache(cm, SETTING_CACHE,jcacheConfiguration);

            // Common Group 기본 TTL 적용
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), jcacheConfiguration);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", jcacheConfiguration);

            createCache(cm, COMMON_GROUP_CACHE, jcacheConfiguration);
            createCache(cm, COMMON_GROUP_LIST_CACHE, jcacheConfiguration);
            createCache(cm, COMMON_DETAIL_CACHE, jcacheConfiguration);
            createCache(cm, COMMON_DETAIL_LIST_BY_GROUP_CACHE, jcacheConfiguration);

            // Tag
            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_TAG_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_TAG_ALL, jcacheConfiguration);
            createCache(cm, CACHE_TAG_PREFIX, jcacheConfiguration);
            createCache(cm, CACHE_TAG_POPULAR, jcacheConfiguration);

            // Upload
            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_ALL, jcacheConfiguration);
            createCache(cm, CACHE_UPLOAD_STATS, jcacheConfiguration);

            // Comment
            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_ID, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_SEARCH, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_BOARD, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_COUNT_BY_USER, jcacheConfiguration);
            createCache(cm, CACHE_COMMENT_STATS, jcacheConfiguration);

            // Board
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

            // BoardTag
            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), jcacheConfiguration);;
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".boardTags", jcacheConfiguration);

            // -----------------------------------------------------------------
            // Hibernate Query Cache (기본 유지)
            // -----------------------------------------------------------------
            createCache(cm, "default-update-timestamps-region", jcacheConfiguration);
            createCache(cm, "default-query-results-region", jcacheConfiguration);


            // jhipster-needle-redis-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName, javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }

    @Autowired(required = false)
    public void setGitProperties(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
    }

    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new PrefixedKeyGenerator(this.gitProperties, this.buildProperties);
    }

    /**
     * TTL(만료 시간)을 개별적으로 설정한 캐시 설정을 생성합니다.
     * 캐시 통계를 활성화하여 모니터링에 활용할 수 있습니다.
     *
     * @param duration TTL 시간값
     * @param unit TTL 단위 (예: TimeUnit.MINUTES)
     */
    private javax.cache.configuration.Configuration<Object, Object> buildTTLConfig(long duration, TimeUnit unit) {
        MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
        config.setStatisticsEnabled(true); // 캐시 모니터링 활성화
        config.setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(new javax.cache.expiry.Duration(unit, duration))
        );
        return config;
    }
}
