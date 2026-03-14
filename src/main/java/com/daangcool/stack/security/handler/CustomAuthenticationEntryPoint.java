package com.daangcool.stack.security.handler;

import com.daangcool.stack.common.constant.ErrorConstants;
import com.daangcool.stack.common.util.ProblemUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 미인증 사용자가 보호 리소스 접근 시 401 응답을 ProblemDetail로 반환.
 * - ProblemUtils.build 사용: status/path/timestamp/locale 자동 포함
 * - ErrorConstants.UNAUTHORIZED_TYPE 사용으로 type URI 일관화
 * - WWW-Authenticate 헤더 추가 (OAuth2/JWT 표준 관례)
 * - 민감한 토큰 문자열 마스킹
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException ex
    ) throws IOException {

        String reason = extractAuthReason(ex);
        log.warn("[401] Unauthorized: uri={}, reason={}", request.getRequestURI(), reason);

        ProblemDetail problem = ProblemUtils.build(
            HttpStatus.UNAUTHORIZED,
            ErrorConstants.UNAUTHORIZED_TYPE.toString(),
            "problem.unauthorized",
            "problem.unauthorized.detail",
            request
        );

        if (reason != null && !reason.isBlank()) {
            problem.setProperty("authReason", reason);
        }

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, buildWwwAuthenticate(reason));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    /** 예외 메시지에서 민감정보 제거 후 사용자에게 노출 가능한 요약 사유 생성 */
    private String extractAuthReason(AuthenticationException ex) {
        if (ex == null) {
            return null;
        }
        String msg = ex.getMessage();
        if ((msg == null || msg.isBlank()) && ex.getCause() != null) {
            msg = ex.getCause().getMessage();
        }
        if (msg == null) {
            return null;
        }

        // 토큰 원문 노출 방지
        msg = msg.replaceAll("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", "Bearer ***");

        // 내부 패키지명/스택 단서 제거
        if (msg.contains("org.") || msg.contains("java.")) {
            return "Invalid or expired token";
        }
        return msg;
    }

    /** RFC6750 관례에 따라 WWW-Authenticate 챌린지 구성 */
    private String buildWwwAuthenticate(String reason) {
        String scheme = "Bearer";
        if (reason == null || reason.isBlank()) {
            return scheme;
        }
        String desc = reason.replace("\"", "'");
        return scheme + " error=\"invalid_token\", error_description=\"" + desc + "\"";
    }
}
