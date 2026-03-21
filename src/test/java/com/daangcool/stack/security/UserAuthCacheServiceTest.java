package com.daangcool.stack.security;

import com.daangcool.stack.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserAuthCacheService 단위 테스트
 * Redis 의존성 Mockito 대체, ApplicationProperties 직접 생성으로 TTL 주입.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthCacheServiceTest {

    @Mock
    private RedissonClient redissonJsonClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RBucket bucket;

    // ApplicationProperties + AuthCache 를 직접 생성하여 ttlMinutes 주입
    private UserAuthCacheService userAuthCacheService;

    private UserAuthCacheDto sampleDto;

    @BeforeEach
    void setUp() {
        ApplicationProperties props = new ApplicationProperties();
        props.getAuthCache().setTtlMinutes(5L);
        userAuthCacheService = new UserAuthCacheService(redissonJsonClient, props);

        sampleDto = new UserAuthCacheDto(
            1L, "testuser", "encodedpwd", "test@example.com",
            true, true, true,
            Set.of("ROLE_USER")
        );
    }

    // ──────────────────────────────────────────────
    // get()
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("get() - Redis 에 값이 있으면 Optional 반환 (Cache HIT)")
    @SuppressWarnings("unchecked")
    void get_CacheHit_ReturnsDto() {
        when(redissonJsonClient.getBucket(UserAuthCacheService.KEY_PREFIX + "testuser")).thenReturn(bucket);
        when(bucket.get()).thenReturn(sampleDto);

        Optional<UserAuthCacheDto> result = userAuthCacheService.get("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().login()).isEqualTo("testuser");
        assertThat(result.get().authorities()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("get() - Redis 에 값이 없으면 Optional.empty() 반환 (Cache MISS)")
    @SuppressWarnings("unchecked")
    void get_CacheMiss_ReturnsEmpty() {
        when(redissonJsonClient.getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(null);

        Optional<UserAuthCacheDto> result = userAuthCacheService.get("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("get() - Redis 장애 시 Optional.empty() fallback (서비스 중단 없음)")
    @SuppressWarnings("unchecked")
    void get_RedisException_ReturnsFallback() {
        when(redissonJsonClient.getBucket(anyString())).thenThrow(new RuntimeException("Redis 연결 실패"));

        Optional<UserAuthCacheDto> result = userAuthCacheService.get("testuser");

        assertThat(result).isEmpty(); // 예외를 던지지 않고 empty 반환
    }

    // ──────────────────────────────────────────────
    // put()
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("put() - 정상 저장 시 RBucket.set() 1회 호출")
    @SuppressWarnings("unchecked")
    void put_ValidDto_StoresCacheWithTtl() {
        when(redissonJsonClient.getBucket(UserAuthCacheService.KEY_PREFIX + "testuser")).thenReturn(bucket);

        userAuthCacheService.put("testuser", sampleDto);

        verify(bucket, times(1)).set(eq(sampleDto), eq(Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("put() - Redis 장애 시 예외를 삼키고 정상 종료 (서비스 중단 없음)")
    @SuppressWarnings("unchecked")
    void put_RedisException_DoesNotThrow() {
        when(redissonJsonClient.getBucket(anyString())).thenThrow(new RuntimeException("Redis 연결 실패"));

        // 예외가 전파되지 않아야 함
        userAuthCacheService.put("testuser", sampleDto);
    }

    // ──────────────────────────────────────────────
    // evict()
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("evict() - 정상 삭제 시 RBucket.delete() 1회 호출")
    @SuppressWarnings("unchecked")
    void evict_ValidLogin_DeletesCache() {
        when(redissonJsonClient.getBucket(UserAuthCacheService.KEY_PREFIX + "testuser")).thenReturn(bucket);
        when(bucket.delete()).thenReturn(true);

        userAuthCacheService.evict("testuser");

        verify(bucket, times(1)).delete();
    }

    @Test
    @DisplayName("evict() - blank login 은 Redis 호출 없이 조기 종료")
    void evict_BlankLogin_DoesNotCallRedis() {
        userAuthCacheService.evict("  ");
        verifyNoInteractions(redissonJsonClient);
    }

    @Test
    @DisplayName("evict() - null login 은 Redis 호출 없이 조기 종료")
    void evict_NullLogin_DoesNotCallRedis() {
        userAuthCacheService.evict(null);
        verifyNoInteractions(redissonJsonClient);
    }

    @Test
    @DisplayName("evict() - Redis 장애 시 예외를 삼키고 정상 종료 (서비스 중단 없음)")
    void evict_RedisException_DoesNotThrow() {
        when(redissonJsonClient.getBucket(anyString())).thenThrow(new RuntimeException("Redis 연결 실패"));

        // 예외가 전파되지 않아야 함
        userAuthCacheService.evict("testuser");
    }
}
