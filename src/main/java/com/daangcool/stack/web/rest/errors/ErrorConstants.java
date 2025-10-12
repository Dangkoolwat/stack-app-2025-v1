package com.daangcool.stack.web.rest.errors;

import java.net.URI;

/**
 * ErrorConstants
 * -----------------------------------------------------------
 * RFC 7807 (Problem Detail for HTTP APIs) 표준에 따른
 * 오류 식별자(URI) 상수를 정의합니다.
 *
 * 각 상수는 클라이언트가 문제 유형을 기계적으로 구분할 수 있도록
 * 고유한 URI 형태로 표현되며, 오류 응답의 "type" 필드에 사용됩니다.
 *
 * 예:
 * {
 *   "type": "https://stack-app.com/problem/entity-not-found",
 *   "title": "Entity not found",
 *   "status": 404,
 *   "detail": "게시글을 찾을 수 없습니다."
 * }
 * -----------------------------------------------------------
 */
public final class ErrorConstants {

    private ErrorConstants() {
        // 상수 전용 클래스이므로 인스턴스화 방지
    }

    /** 문제 유형 URI의 공통 기본 경로 */
    public static final String PROBLEM_BASE_URL = "https://stack-app.com/problem";

    // -----------------------------------------------------
    // 공통 / 기본 오류 유형
    // -----------------------------------------------------

    /** 기본 문제 유형 (Generic problem with message) */
    public static final URI DEFAULT_TYPE =
        URI.create(PROBLEM_BASE_URL + "/problem-with-message");

    /** 잘못된 요청 (잘못된 입력값, 누락된 필드 등) */
    public static final URI BAD_REQUEST_TYPE =
        URI.create(PROBLEM_BASE_URL + "/bad-request");

    /** 제약 조건 위반 (유효성 검증 실패, @Valid, @NotNull 등) */
    public static final URI CONSTRAINT_VIOLATION_TYPE =
        URI.create(PROBLEM_BASE_URL + "/constraint-violation");

    /** 접근 권한 없음 (Spring Security AccessDeniedException) */
    public static final URI ACCESS_DENIED_TYPE =
        URI.create(PROBLEM_BASE_URL + "/access-denied");

    // -----------------------------------------------------
    // 인증 및 사용자 관련 오류
    // -----------------------------------------------------

    /** 로그인 아이디 중복 */
    public static final URI LOGIN_ALREADY_USED_TYPE =
        URI.create(PROBLEM_BASE_URL + "/login-used");

    /** 이메일 중복 */
    public static final URI EMAIL_ALREADY_USED_TYPE =
        URI.create(PROBLEM_BASE_URL + "/email-used");

    /** 비밀번호 유효성 실패 */
    public static final URI INVALID_PASSWORD_TYPE =
        URI.create(PROBLEM_BASE_URL + "/invalid-password");

    // -----------------------------------------------------
    // 도메인 / 엔티티 관련 오류
    // -----------------------------------------------------

    /** 요청한 엔티티(데이터)를 찾을 수 없음 */
    public static final URI ENTITY_NOT_FOUND_TYPE =
        URI.create(PROBLEM_BASE_URL + "/entity-not-found");

    // -----------------------------------------------------
    // 파일 시스템 / 스토리지 관련 오류
    // -----------------------------------------------------

    /** 요청한 파일을 찾을 수 없음 */
    public static final URI FILE_NOT_FOUND_TYPE =
        URI.create(PROBLEM_BASE_URL + "/file-not-found");

    /** 파일 저장소 처리 중 오류 (I/O, 권한 문제 등) */
    public static final URI FILE_STORAGE_ERROR_TYPE =
        URI.create(PROBLEM_BASE_URL + "/file-storage-error");

    /** 인증 실패 (401 Unauthorized) */
    public static final URI UNAUTHORIZED_TYPE =
        URI.create(PROBLEM_BASE_URL + "/unauthorized");
}
