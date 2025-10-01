package com.daangcool.stack.web.rest.errors;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * RFC 7807 기반 ProblemDetail 헬퍼
 *
 * - 모든 예외 응답에서 공통 확장 필드(timestamp, path)를 보장
 * - null properties 문제가 발생하지 않도록 최소한 빈 객체 이상 유지
 */
public final class ProblemUtils {

    // 한국 시간대 (Asia/Seoul)
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private ProblemUtils() {}

    public static ProblemDetail build(HttpStatus status,
                                      String type,
                                      String title,
                                      String detail,
                                      HttpServletRequest req) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(req.getRequestURI()));

        // 공통 확장 필드 항상 추가
//        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        // ✅ 한국 시간 (KST, UTC+9) 고정
        problem.setProperty("timestamp", OffsetDateTime.now(KOREA_ZONE).toString());
        problem.setProperty("path", req.getRequestURI());

        return problem;
    }
}

