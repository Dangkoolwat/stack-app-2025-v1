package com.daangcool.stack.common.exception;

import com.daangcool.stack.common.constant.ErrorConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

/**
 * 유효하지 않은 파일 업로드 시도시 발생하는 예외 (MIME 타입 미일치, 허용되지 않은 확장자 등).
 * 400 Bad Request와 RFC 7807 형식을 반환합니다. (C-4)
 */
@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class InvalidFileException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public InvalidFileException(String detail) {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withType(ErrorConstants.INVALID_FILE_TYPE)
                .withTitle("Invalid File")
                .withDetail(detail)
                .build(),
            null
        );
    }
}
