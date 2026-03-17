package com.daangcool.stack.web.filter;

import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.security.RateLimitingRegistry;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RateLimitingFilter 단위 테스트 (W-1)
 * ------------------------------------------------------------------
 * Filter를 단독으로 테스트합니다 (Spring Context 불필요).
 * MockHttpServletRequest/Response를 사용하여 Rate Limiting 동작을 검증합니다.
 * ------------------------------------------------------------------
 */
class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private ApplicationProperties applicationProperties;
    private RateLimitingRegistry registry;

    @Mock
    private ProxyManager<String> proxyManager;

    @Mock
    private RemoteBucketBuilder<String> bucketBuilder;

    private final Map<String, BucketProxy> bucketCache = new ConcurrentHashMap<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        applicationProperties = new ApplicationProperties();
        bucketCache.clear();
        
        // ProxyManager Mock 설정
        when(proxyManager.builder()).thenReturn(bucketBuilder);
        
        // BucketProxy Mock 설정 (상태 유지 및 ClassCastException 방지)
        when(bucketBuilder.build(anyString(), any(Supplier.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return bucketCache.computeIfAbsent(key, k -> {
                Supplier<BucketConfiguration> supplier = invocation.getArgument(1);
                BucketConfiguration config = supplier.get();
                
                // 실제 로직을 수행할 로컬 버킷 생성
                var builder = Bucket.builder();
                for (Bandwidth bandwidth : config.getBandwidths()) {
                    builder.addLimit(bandwidth);
                }
                Bucket realBucket = builder.build();
                
                BucketProxy mockProxy = mock(BucketProxy.class);
                when(mockProxy.tryConsume(anyLong())).thenAnswer(i -> realBucket.tryConsume(i.getArgument(0)));
                when(mockProxy.tryConsumeAndReturnRemaining(anyLong())).thenAnswer(i -> realBucket.tryConsumeAndReturnRemaining(i.getArgument(0)));
                when(mockProxy.asVerbose()).thenReturn(realBucket.asVerbose());
                return mockProxy;
            });
        });

        // registry.clear() 호출 시 내부 캐시도 비우도록 registry를 익명 클래스로 재정의 (테스트용)
        registry = new RateLimitingRegistry(proxyManager) {
            @Override
            public void clear() {
                super.clear();
                bucketCache.clear();
            }
        };
        filter = new RateLimitingFilter(new ObjectMapper(), applicationProperties, registry);
    }

    @Test
    @DisplayName("Registry를 초기화하면 다시 요청이 허용된다")
    void shouldAllowRequestsAfterRegistryClear() throws Exception {
        // 1. 요청 차단될 때까지 재차 호출 (authenticate: 10회)
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(postRequest("/api/authenticate"), new MockHttpServletResponse(), new MockFilterChain());
        }
        var blockedRes = new MockHttpServletResponse();
        filter.doFilterInternal(postRequest("/api/authenticate"), blockedRes, new MockFilterChain());
        assertThat(blockedRes.getStatus()).isEqualTo(429);

        // 2. Registry 초기화
        registry.clear();

        // 3. 다시 요청 시 200 응답 확인
        var passedRes = new MockHttpServletResponse();
        filter.doFilterInternal(postRequest("/api/authenticate"), passedRes, new MockFilterChain());
        assertThat(passedRes.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("OTP 요청(/api/auth/email/request)도 정상적으로 제한된다")
    void shouldLimitOtpRequestEndpoints() throws Exception {
        // 기본값: 5회/10분
        for (int i = 0; i < 5; i++) {
            var req = postRequest("/api/auth/email/request");
            var res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        var req = postRequest("/api/auth/email/request");
        var res = new MockHttpServletResponse();
        filter.doFilterInternal(req, res, new MockFilterChain());

        assertThat(res.getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("한도 이내 요청은 정상 통과하고 X-Rate-Limit-Remaining 헤더를 반환한다")
    void shouldPassWithinLimit() throws Exception {
        var request = postRequest("/api/authenticate");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNotNull();
    }

    @Test
    @DisplayName("한도 초과 시 429 Too Many Requests를 반환한다")
    void shouldReturn429WhenLimitExceeded() throws Exception {
        // /api/authenticate 한도: 10회/5분
        for (int i = 0; i < 10; i++) {
            var req = postRequest("/api/authenticate");
            var res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 11번째 요청 → 429
        var req = postRequest("/api/authenticate");
        var res = new MockHttpServletResponse();
        filter.doFilterInternal(req, res, new MockFilterChain());

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isNotNull();
        assertThat(res.getContentType()).contains("application/problem+json");
    }

    @Test
    @DisplayName("다른 IP는 독립적으로 카운트된다")
    void shouldCountPerIpIndependently() throws Exception {
        // IP-A로 10회 소진
        for (int i = 0; i < 10; i++) {
            var req = postRequest("/api/authenticate");
            req.setRemoteAddr("1.1.1.1");
            filter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        // IP-B는 여전히 가능
        var req = postRequest("/api/authenticate");
        req.setRemoteAddr("2.2.2.2");
        var res = new MockHttpServletResponse();
        filter.doFilterInternal(req, res, new MockFilterChain());

        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("GET 요청은 Rate Limiting 대상이 아니다")
    void shouldNotLimitGetRequests() throws Exception {
        var req = new MockHttpServletRequest("GET", "/api/authenticate");
        var res = new MockHttpServletResponse();

        filter.doFilterInternal(req, res, new MockFilterChain());

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(res.getHeader("X-Rate-Limit-Remaining")).isNull();
    }

    @Test
    @DisplayName("Rate Limiting 대상 외 경로는 무제한이다")
    void shouldNotLimitNonTargetPaths() throws Exception {
        for (int i = 0; i < 100; i++) {
            var req = postRequest("/api/some-other-endpoint");
            var res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP를 기준으로 Rate Limiting한다")
    void shouldUseXForwardedForHeader() throws Exception {
        // X-Forwarded-For IP로 10회 소진
        for (int i = 0; i < 10; i++) {
            var req = postRequest("/api/authenticate");
            req.setRemoteAddr("127.0.0.1");
            req.addHeader("X-Forwarded-For", "10.0.0.1, 127.0.0.1");
            filter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        // 같은 X-Forwarded-For IP → 차단
        var req1 = postRequest("/api/authenticate");
        req1.setRemoteAddr("127.0.0.1");
        req1.addHeader("X-Forwarded-For", "10.0.0.1, 127.0.0.1");
        var res1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, res1, new MockFilterChain());
        assertThat(res1.getStatus()).isEqualTo(429);

        // 다른 X-Forwarded-For IP → 통과
        var req2 = postRequest("/api/authenticate");
        req2.setRemoteAddr("127.0.0.1");
        req2.addHeader("X-Forwarded-For", "10.0.0.2, 127.0.0.1");
        var res2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, res2, new MockFilterChain());
        assertThat(res2.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("각 엔드포인트는 독립적인 Rate Limit을 가진다")
    void shouldHaveIndependentLimitsPerEndpoint() throws Exception {
        // /api/authenticate 10회 소진
        for (int i = 0; i < 10; i++) {
            var req = postRequest("/api/authenticate");
            filter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        // /api/register는 여전히 사용 가능 (별도 카운트)
        var req = postRequest("/api/register");
        var res = new MockHttpServletResponse();
        filter.doFilterInternal(req, res, new MockFilterChain());
        assertThat(res.getStatus()).isEqualTo(200);
    }

    private MockHttpServletRequest postRequest(String path) {
        var request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
