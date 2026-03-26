package com.daangcool.stack.common.exception;


import com.daangcool.stack.common.constant.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * 400 Bad Request 상황에서 사용되는 공통 예외 클래스.
 * - 주로 잘못된 입력 데이터나 검증 실패 시 발생합니다.
 * - Entity 이름과 오류 키를 함께 내려 클라이언트 단에서 메시지 매핑을 쉽게 합니다.
 */
public class BadRequestAlertException extends RuntimeException {

    private final URI type;
    private final String entityName;
    private final String errorKey;

    public BadRequestAlertException(URI type, String message, String entityName, String errorKey) {
        super(message);
        this.type = type;
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public BadRequestAlertException(String message, String entityName, String errorKey) {
        this(ErrorConstants.BAD_REQUEST_TYPE, message, entityName, errorKey);
    }

    public ProblemDetail toProblemDetail(String instance) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(type);
        problem.setTitle("Bad Request");
        problem.setDetail(getMessage());
        problem.setInstance(URI.create(instance));
        problem.setProperty("entityName", entityName);
        problem.setProperty("errorKey", errorKey);
        problem.setProperty("timestamp", java.time.OffsetDateTime.now().toString());
        return problem;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public URI getType() {
        return type;
    }
}
