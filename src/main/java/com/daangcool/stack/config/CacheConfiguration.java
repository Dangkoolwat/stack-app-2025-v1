package com.daangcool.stack.config;

import tools.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.jhipster.config.JHipsterProperties;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import java.util.concurrent.TimeUnit;

import static com.daangcool.stack.common.constant.CacheNames.*;


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

    private final ApplicationProperties applicationProperties;

    public CacheConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean(name = {"redissonClient", "redissonJsonClient"}, destroyMethod = "shutdown")
    @org.springframework.context.annotation.Primary
    public RedissonClient redissonClient(JHipsterProperties jHipsterProperties, ObjectMapper objectMapper) {
        // DTO 중심 애플리케이션 캐시와 Redis 운영성 기능(락/레이트리밋/OTP)에서 공통 재사용합니다.
        Config config = getRedissonConfig(jHipsterProperties);

        tools.jackson.databind.module.SimpleModule simpleKeyModule = new tools.jackson.databind.module.SimpleModule();
        simpleKeyModule.addDeserializer(org.springframework.cache.interceptor.SimpleKey.class, new SimpleKeyDeserializer());

        ObjectMapper redisMapper = ((tools.jackson.databind.cfg.MapperBuilder<?, ?>) objectMapper.rebuild())
            .addModule(simpleKeyModule) // [FIX] Jackson 3 SimpleKey 대응 (Deserializer 사용)
            .build();

        config.setCodec(new org.redisson.codec.JsonJackson3Codec(redisMapper));
        return Redisson.create(config);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(
        RedissonClient redissonClient
    ) {
        return buildTTLConfig(redissonClient, applicationProperties.getCache().getTtl().getDefaultSeconds(), TimeUnit.SECONDS);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(
        RedissonClient redissonClient
    ) {
        javax.cache.configuration.Configuration<Object, Object> applicationDefaultConfig = buildTTLConfig(
            redissonClient,
            applicationProperties.getCache().getTtl().getDefaultSeconds(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> applicationLongConfig = buildTTLConfig(
            redissonClient,
            applicationProperties.getCache().getTtl().getLongSeconds(),
            TimeUnit.SECONDS
        );

        return cm -> {
            createCache(cm, SETTINGS, applicationLongConfig);
            createCache(cm, COMMON_GROUPS, applicationLongConfig);
            createCache(cm, COMMON_GROUP_LIST, applicationLongConfig);
            createCache(cm, COMMON_DETAILS, applicationLongConfig);
            createCache(cm, COMMON_DETAILS_BY_GROUP, applicationLongConfig);

            createCache(cm, BOARD_BY_ID, applicationDefaultConfig);
            createCache(cm, BOARD_PAGE, applicationDefaultConfig);
            createCache(cm, BOARD_SEARCH, applicationDefaultConfig);
            createCache(cm, BOARD_NOTICES, applicationDefaultConfig);
            createCache(cm, BOARD_COUNT_TOTAL, applicationDefaultConfig);
            createCache(cm, BOARD_COUNT_BY_USER, applicationDefaultConfig);

            createCache(cm, COMMENT_BY_ID, applicationDefaultConfig);
            createCache(cm, COMMENT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, COMMENT_SEARCH, applicationDefaultConfig);
            createCache(cm, COMMENT_COUNT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, COMMENT_COUNT_BY_USER, applicationDefaultConfig);
            createCache(cm, COMMENT_STATS, applicationDefaultConfig);

            createCache(cm, TAG_BY_ID, applicationLongConfig);
            createCache(cm, TAG_ALL, applicationLongConfig);
            createCache(cm, TAG_PREFIX, applicationLongConfig);
            createCache(cm, TAG_POPULAR, applicationDefaultConfig);

            createCache(cm, UPLOAD_BY_ID, applicationDefaultConfig);
            createCache(cm, UPLOAD_BY_BOARD, applicationDefaultConfig);
            createCache(cm, UPLOAD_ALL, applicationDefaultConfig);
            createCache(cm, UPLOAD_STATS, applicationDefaultConfig);
        };
    }

    private void createCache(
        javax.cache.CacheManager cm,
        String cacheName,
        javax.cache.configuration.Configuration<Object, Object> config
    ) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache == null) {
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

    /**
     * [FIX] Jackson 3 SimpleKey 대응을 위한 커스텀 역직렬화기
     * SimpleKey는 기본 생성자가 없고, params가 null일 경우 IllegalArgumentException을 던지므로
     * 이를 안전하게 처리하기 위한 Deserializer를 구현합니다.
     */
    public static class SimpleKeyDeserializer extends tools.jackson.databind.ValueDeserializer<org.springframework.cache.interceptor.SimpleKey> {
        @Override
        public org.springframework.cache.interceptor.SimpleKey deserialize(tools.jackson.core.JsonParser p, tools.jackson.databind.DeserializationContext ctxt) throws tools.jackson.core.JacksonException {
            tools.jackson.databind.JsonNode node = ctxt.readTree(p);
            tools.jackson.databind.JsonNode paramsNode = node.get("params");
            if (paramsNode == null || paramsNode.isNull() || !paramsNode.isArray()) {
                return new org.springframework.cache.interceptor.SimpleKey();
            }
            
            // paramsNode를 Object[]로 역직렬화
            Object[] params = ctxt.readTreeAsValue(paramsNode, Object[].class);
            return new org.springframework.cache.interceptor.SimpleKey(params);
        }
    }

}
