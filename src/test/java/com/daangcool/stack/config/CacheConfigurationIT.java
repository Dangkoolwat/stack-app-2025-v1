package com.daangcool.stack.config;

import com.daangcool.stack.common.constant.CacheNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheConfigurationIT {

    private CacheConfiguration cacheConfiguration;
    private RedissonClient redissonClient;
    private javax.cache.CacheManager jCacheManager;
    private javax.cache.Cache<Object, Object> existingCache;

    @BeforeEach
    void setUp() {
        ApplicationProperties applicationProperties = new ApplicationProperties();
        cacheConfiguration = new CacheConfiguration(applicationProperties);
        redissonClient = mock(RedissonClient.class);
        jCacheManager = mock(javax.cache.CacheManager.class);
        existingCache = mock(javax.cache.Cache.class);
    }

    @Test
    void cacheManagerCustomizer_ShouldCreateMissingCachesWithoutClearingExistingOnes() {
        when(jCacheManager.getCache(CacheNames.SETTINGS)).thenReturn(existingCache);

        JCacheManagerCustomizer customizer = cacheConfiguration.cacheManagerCustomizer(redissonClient);
        customizer.customize(jCacheManager);

        verify(existingCache, never()).clear();
        verify(jCacheManager, never()).createCache(eq(CacheNames.SETTINGS), any(javax.cache.configuration.Configuration.class));
        verify(jCacheManager, times(24)).createCache(any(String.class), any(javax.cache.configuration.Configuration.class));
    }
}
