package com.daangcool.stack.security.handler;

import com.daangcool.stack.web.rest.errors.ProblemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 기본 메시지
        String jwtMessage = null;
        if (authException != null) {
            // 예외 메시지 혹은 원인 메시지를 추출
            if (authException.getMessage() != null && !authException.getMessage().isBlank()) {
                jwtMessage = authException.getMessage();
            } else if (authException.getCause() != null && authException.getCause().getMessage() != null) {
                jwtMessage = authException.getCause().getMessage();
            }
        }


        // detail은 고정, JWT 오류는 확장 프로퍼티로 추가
        ProblemDetail problem = ProblemUtils.build(
            HttpStatus.UNAUTHORIZED,
            "https://stack-app.com/probs/auth",
            "Unauthorized",
            "Full authentication is required to access this resource",
            request
        );
        if (jwtMessage != null && !jwtMessage.isBlank()) {
            problem.setProperty("jwtError", jwtMessage);
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);

    }
}
