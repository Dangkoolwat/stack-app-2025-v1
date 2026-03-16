package com.daangcool.stack.web.filter;

import com.daangcool.stack.common.constant.ErrorConstants;
import com.daangcool.stack.common.util.ProblemUtils;
import com.daangcool.stack.config.ApplicationProperties;
import com.daangcool.stack.security.RateLimitingRegistry;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limiting Filter (W-1 보안 개선)
 * ------------------------------------------------------------------
 * 공개 인증 및 보안에 민감한 엔드포인트에 대해 IP 기반 요청 횟수 제한을 적용합니다.
 * 본 필터는 Spring Security 필터 체인의 최상단에 위치하여 인증 시도 전 공격을 차단합니다.
 *
 * 주요 특징:
 * - Token Bucket 알고리즘 (Bucket4j) 사용
 * - Redis 기반 분산 저장 지원 (여러 서버 인스턴스 간 상태 공유)
 * - 엔드포인트별 독립 정책 (IP + 경로 조합 키)
 * - 차단 시 RFC 7807 ProblemDetail 형식 429 응답 반환
 * - X-Forwarded-For 헤더 지원 (L4/L7 프록시 서버 환경 대응)
 *
 * @author Antigravity
 * ------------------------------------------------------------------
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimitingRegistry registry;
    private final ObjectMapper objectMapper;
    private final ApplicationProperties applicationProperties;
    private final Map<String, ApplicationProperties.RateLimit.Policy> policies = new HashMap<>();

    /**
     * 필터 생성자
     *
     * @param objectMapper           에러 응답 JSON 직렬화를 위한 매퍼
     * @param applicationProperties  엔드포인트별 수치 설정 (tokens, duration)
     * @param registry               버킷 상태 관리를 위한 레지스트리 (Redis 연동 포함)
     */
    public RateLimitingFilter(
        ObjectMapper objectMapper,
        ApplicationProperties applicationProperties,
        RateLimitingRegistry registry
    ) {
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
        this.registry = registry;
        initializePolicies();
    }

    /**
     * 프로퍼티에 정의된 엔드포인트별 Rate Limit 정책을 로드합니다.
     */
    private void initializePolicies() {
        var rl = applicationProperties.getRateLimit();
        policies.put("/api/authenticate", rl.getAuthenticate());
        policies.put("/api/register", rl.getRegister());
        policies.put("/api/account/reset-password/init", rl.getResetPasswordInit());
        policies.put("/api/auth/email/request", rl.getOtpRequest());
        policies.put("/api/auth/email/verify", rl.getOtpVerify());
    }

    /**
     * HTTP 요청을 가로채어 Rate Limiting을 수행합니다.
     *
     * @param request       HTTP 요청
     * @param response      HTTP 응답
     * @param filterChain   필터 체인
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      I/O 예외 발생 시
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // POST 요청만 Rate Limiting 대상 (인증/가입/OTP 요청 등은 모두 POST 방식임)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        ApplicationProperties.RateLimit.Policy policy = policies.get(path);
        
        // 현재 경로가 Rate Limiting 정책 대상이 아닐 경우 바로 다음 필터로 이동
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 클라이언트 식별 정보 (IP) 추출 및 버킷 키 생성
        String clientIp = resolveClientIp(request);
        String bucketKey = clientIp + ":" + path;

        // 분산 환경(Redis) 대응을 위해 정책 설정을 Registry로 전달하여 버킷을 가져옴
        BucketConfiguration bucketConfiguration = BucketConfiguration.builder()
            .addLimit(Bandwidth.builder()
                .capacity(policy.getTokens())
                .refillGreedy(policy.getTokens(), Duration.ofMinutes(policy.getDurationMinutes()))
                .build())
            .build();
        
        Bucket bucket = registry.getBucket(bucketKey, bucketConfiguration);

        // 토큰 1개 소모 시도 (tryConsume은 소모 성공 여부를 반환함)
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // 요청 허용: 잔여 토큰 정보를 헤더(X-Rate-Limit-Remaining)에 포함하여 응답
            response.setHeader("X-Rate-Limit-Remaining",
                String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // 요청 거부: 한도를 초과함 (429 Too Many Requests)
            // 재도전 가능 시간(초) 계산
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000 + 1;
            log.warn("[429] Rate limit exceeded: ip={}, path={}, retryAfter={}s",
                clientIp, path, retryAfterSeconds);

            // RFC 7807 표준 에러 응답 기록
            writeRateLimitResponse(request, response, retryAfterSeconds);
        }
    }

    /**
     * Reverse Proxy (L4, CloudFlare 등) 뒤에서 실제 클라이언트 IP 추출.
     * X-Forwarded-For 헤더의 첫 번째 값을 우선 사용합니다.
     *
     * @param request HTTP 요청
     * @return 식별된 클라이언트 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 429 Too Many Requests 응답을 RFC 7807 (ProblemDetail) 형식으로 기록합니다.
     * 필터 레벨에서 발생하므로 CustomAuthenticationEntryPoint와 동일하게 
     * ObjectMapper를 사용하여 직접 Response Body를 작성합니다.
     *
     * @param request           HTTP 요청
     * @param response          HTTP 응답
     * @param retryAfterSeconds 다시 시도 가능한 대기 시간 (초)
     * @throws IOException I/O 예외 발생 시
     */
    private void writeRateLimitResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {
        var problem = ProblemUtils.build(
            HttpStatus.TOO_MANY_REQUESTS,
            ErrorConstants.TOO_MANY_REQUESTS_TYPE.toString(),
            "Too Many Requests",
            "요청 횟수 제한을 초과했습니다. " + retryAfterSeconds + "초 후 다시 시도해 주세요.",
            request
        );
        problem.setProperty("retryAfterSeconds", retryAfterSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        // 클라이언트에게 대기 시간을 알리는 표준 헤더
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
