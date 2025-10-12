package com.daangcool.stack.web.exception;

/**
 * 파일 업로드 중 발생하는 예외를 표현하는 사용자 정의 런타임 예외 클래스.
 *
 * 사용 목적:
 * - StorageService, UploadService, UploadFileUtils 등에서 발생한 파일 저장 오류를 표현
 * - ExceptionTranslator에서 RFC7807 형식(JSON ProblemDetail)으로 자동 변환
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
