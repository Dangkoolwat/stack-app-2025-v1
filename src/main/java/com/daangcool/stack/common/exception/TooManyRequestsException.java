package com.daangcool.stack.common.exception;

import com.daangcool.stack.common.constant.ErrorConstants;
import com.daangcool.stack.common.util.ProblemUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Too Many Requests (429) 예외 클래스 (W-1)
 * ------------------------------------------------------------------
 * Rate Limiting 임계치를 초과했을 때 서비스 계층이나 필터에서 발생하는 예외입니다.
 * RFC 7807 (Problem Detail for HTTP APIs) 형식을 따르며, 
 * 클라이언트에게 재시도 대기 시간을 알리는 정보를 포함합니다.
 * ------------------------------------------------------------------
 */
public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    /**
     * 예외 생성자
     *
     * @param retryAfterSeconds 다시 시도하기까지 기다려야 하는 시간 (초)
     */
    public TooManyRequestsException(long retryAfterSeconds) {
        super("Too Many Requests. Please retry after " + retryAfterSeconds + " seconds.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * 대기 시간(초)을 반환합니다.
     *
     * @return 재시도 대기 시간
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * 본 예외를 Spring 6 표준 ProblemDetail 객체로 변환합니다.
     *
     * @param instance 요청이 발생한 API 경로 (HttpServletRequest.getRequestURI())
     * @return RFC 7807 규격의 ProblemDetail
     */
    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemUtils.build(
            HttpStatus.TOO_MANY_REQUESTS,
            ErrorConstants.TOO_MANY_REQUESTS_TYPE.toString(),
            "Too Many Requests",
            "요청 횟수 제한을 초과했습니다. " + retryAfterSeconds + "초 후 다시 시도해 주세요.",
            instance
        );
        problem.setProperty("retryAfterSeconds", retryAfterSeconds);
        return problem;
    }

    /**
     * 본 예외를 Spring 6 표준 ProblemDetail 객체로 변환합니다. (instance 생략 버전)
     *
     * @return RFC 7807 규격의 ProblemDetail
     */
    public ProblemDetail toProblemDetail() {
        return toProblemDetail(null);
    }
}
