package com.daangcool.stack.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;


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

        /**
         * [IMPORTANT] Jackson 3 / Redis Cache 설정 (NM-5)
         * ---------------------------------------------------
         * 1. DefaultTyping 활성화: 
         *    JSON 저장 시 타입 정보(@class)를 포함하여 역직렬화 시의 타입 불일치(Map으로 복원됨) 문제를 해결합니다.
         * 2. objectMapper.rebuild() 사용 이유:
         *    Jackson 3의 ObjectMapper는 Immutable하므로, Spring Boot가 구성한 기본 설정(Hibernate7Module, Customizers 등)을 
         *    그대로 상속받기 위해 rebuild()를 사용합니다. 이를 통해 지연 로딩된 컬렉션 직렬화 시 
         *    LazyInitializationException이 발생하는 것을 방지하고 프로젝트의 캐시 정책을 유지합니다.
         * 3. [주의사항] Redis 캐시 초기화 필요:
         *    기존에 타입 정보 없이 저장된 캐시 데이터가 있을 경우 역직렬화 에러가 발생할 수 있습니다.
         *    배포 후 반드시 Redis 캐시 초기화(예: redis-cli flushall)를 수행해야 합니다.
         * ---------------------------------------------------
         */
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();

        tools.jackson.databind.module.SimpleModule simpleKeyModule = new tools.jackson.databind.module.SimpleModule();
        simpleKeyModule.addDeserializer(org.springframework.cache.interceptor.SimpleKey.class, new SimpleKeyDeserializer());

        ObjectMapper redisMapper = ((tools.jackson.databind.cfg.MapperBuilder<?, ?>) objectMapper.rebuild())
            .activateDefaultTyping(ptv, tools.jackson.databind.DefaultTyping.NON_FINAL, com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY)
            .addModule(simpleKeyModule) // [FIX] Jackson 3 SimpleKey 대응 (Deserializer 사용)
            .build();

        // [REFAC] JsonJacksonCodec 대신 Jackson 3를 지원하는 JsonJackson3Codec 사용
        config.setCodec(new org.redisson.codec.JsonJackson3Codec(redisMapper));
        return Redisson.create(config);
    }






    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(
        RedissonClient redissonClient,
        JHipsterProperties props
    ) {
        return buildTTLConfig(redissonClient, applicationProperties.getCache().getTtl().getDefaultSeconds(), TimeUnit.SECONDS);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> longTtlCacheConfiguration(RedissonClient redissonClient) {
        return buildTTLConfig(redissonClient, applicationProperties.getCache().getTtl().getLongSeconds(), TimeUnit.SECONDS);
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
        // Spring Caches: Use JSON client (Default TTL / Long TTL)
        javax.cache.configuration.Configuration<Object, Object> applicationDefaultConfig = buildTTLConfig(
            redissonJsonClient,
            applicationProperties.getCache().getTtl().getDefaultSeconds(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> applicationLongConfig = buildTTLConfig(
            redissonJsonClient,
            applicationProperties.getCache().getTtl().getLongSeconds(),
            TimeUnit.SECONDS
        );

        // Hibernate L2 Cache regions: Use Binary client (Default TTL / Long TTL)
        javax.cache.configuration.Configuration<Object, Object> hibernateDefaultConfig = buildTTLConfig(
            redissonClient,
            applicationProperties.getCache().getTtl().getDefaultSeconds(),
            TimeUnit.SECONDS
        );
        javax.cache.configuration.Configuration<Object, Object> hibernateLongConfig = buildTTLConfig(
            redissonClient,
            applicationProperties.getCache().getTtl().getLongSeconds(),
            TimeUnit.SECONDS
        );

        return cm -> {
            // A. 전역 설정 및 공통 코드 (Long TTL)
            createCache(cm, com.daangcool.stack.domain.Settings.class.getName(), hibernateLongConfig);
            createCache(cm, SETTINGS, applicationLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeDetail.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.common.CommonCodeGroup.class.getName() + ".details", hibernateLongConfig);
            createCache(cm, COMMON_GROUPS, applicationLongConfig);
            createCache(cm, COMMON_GROUP_LIST, applicationLongConfig);
            createCache(cm, COMMON_DETAILS, applicationLongConfig);
            createCache(cm, COMMON_DETAILS_BY_GROUP, applicationLongConfig);

            // B. 게시판 (Board) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName(), hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".comments", hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".attachments", hibernateDefaultConfig);
            createCache(cm, com.daangcool.stack.domain.board.Board.class.getName() + ".boardTags", hibernateDefaultConfig);
            createCache(cm, BOARD_BY_ID, applicationDefaultConfig);
            createCache(cm, BOARD_PAGE, applicationDefaultConfig);
            createCache(cm, BOARD_SEARCH, applicationDefaultConfig);
            createCache(cm, BOARD_NOTICES, applicationDefaultConfig);
            createCache(cm, BOARD_COUNT_TOTAL, applicationDefaultConfig);
            createCache(cm, BOARD_COUNT_BY_USER, applicationDefaultConfig);

            // C. 댓글 (Comment) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Comment.class.getName(), hibernateDefaultConfig);
            createCache(cm, COMMENT_BY_ID, applicationDefaultConfig);
            createCache(cm, COMMENT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, COMMENT_SEARCH, applicationDefaultConfig);
            createCache(cm, COMMENT_COUNT_BY_BOARD, applicationDefaultConfig);
            createCache(cm, COMMENT_COUNT_BY_USER, applicationDefaultConfig);
            createCache(cm, COMMENT_STATS, applicationDefaultConfig);

            // D. 태그 (Tag) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Tag.class.getName(), hibernateLongConfig);
            createCache(cm, com.daangcool.stack.domain.board.BoardTag.class.getName(), hibernateDefaultConfig);
            createCache(cm, TAG_BY_ID, applicationLongConfig);
            createCache(cm, TAG_ALL, applicationLongConfig);
            createCache(cm, TAG_PREFIX, applicationLongConfig);
            createCache(cm, TAG_POPULAR, applicationDefaultConfig);

            // E. 업로드 (Upload) 서비스 캐시
            createCache(cm, com.daangcool.stack.domain.board.Upload.class.getName(), hibernateDefaultConfig);
            createCache(cm, UPLOAD_BY_ID, applicationDefaultConfig);
            createCache(cm, UPLOAD_BY_BOARD, applicationDefaultConfig);
            createCache(cm, UPLOAD_ALL, applicationDefaultConfig);
            createCache(cm, UPLOAD_STATS, applicationDefaultConfig);

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

    /**
     * [FIX] Jackson 3 PagedModel 대응을 위한 MixIn
     * Spring Data의 PagedModel이 Jackson 2 애노테이션을 가지므로 Jackson 3가 이를 인식하지 못합니다.
     * 따라서 Content와 PageMetadata를 받는 생성자를 Jackson 3 애노테이션으로 매핑해줍니다.
     */
    public abstract static class PagedModelMixIn<T> {
        @com.fasterxml.jackson.annotation.JsonCreator(mode = com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES)
        public PagedModelMixIn(
            @com.fasterxml.jackson.annotation.JsonProperty("content") java.util.List<T> content,
            @com.fasterxml.jackson.annotation.JsonProperty("page") org.springframework.data.web.PagedModel.PageMetadata metadata) {}
    }

}
