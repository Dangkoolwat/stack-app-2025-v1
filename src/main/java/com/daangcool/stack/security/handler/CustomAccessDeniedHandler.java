package com.daangcool.stack.security.handler;

import com.daangcool.stack.util.errors.ErrorConstants;
import com.daangcool.stack.util.errors.ProblemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
/**
 * 인증은 되었으나 권한 없는 리소스 접근 시 403 응답을 ProblemDetail로 반환.
 * - ProblemUtils.build 사용: status/path/timestamp/locale 자동 포함
 * - ErrorConstants.ACCESS_DENIED_TYPE 사용으로 type URI 일관화
 * - 캐시 방지 헤더 추가
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException ex
    ) throws IOException {

        log.warn("[403] Access denied: uri={}, reason={}", request.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemUtils.build(
            HttpStatus.FORBIDDEN,
            ErrorConstants.ACCESS_DENIED_TYPE.toString(),
            "problem.accessDenied",          // i18n title key
            "problem.accessDenied.detail",   // i18n detail key
            request
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");

        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
