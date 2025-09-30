package com.daangcool.stack.web.exception;

import com.daangcool.stack.web.rest.errors.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

public class BadRequestAlertException extends RuntimeException {

    private final URI type;
    private final String entityName;
    private final String errorKey;

    // 전체 파라미터 생성자
    public BadRequestAlertException(URI type, String defaultMessage, String entityName, String errorKey) {
        super(defaultMessage);
        this.type = type;
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    // 편의 생성자 (type은 DEFAULT_TYPE 고정)
    public BadRequestAlertException(String defaultMessage, String entityName, String errorKey) {
        this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey);
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(type);
        problem.setTitle("Bad Request");
        problem.setDetail(getMessage());
        problem.setProperty("instance", instance);

        // 확장 필드
        problem.setProperty("entityName", entityName);
        problem.setProperty("errorKey", errorKey);

        return problem;
    }
}
