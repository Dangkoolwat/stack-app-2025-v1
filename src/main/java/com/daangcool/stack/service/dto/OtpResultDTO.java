package com.daangcool.stack.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OTP 검증 결과 DTO.
 *
 * <p>이메일 인증 요청 및 검증 결과를 클라이언트에 반환합니다.</p>
 *
 * <ul>
 *   <li>success: true/false (성공 여부)</li>
 *   <li>message: 상태 코드 (예: OTP_VERIFIED, INVALID_CODE, OTP_EXPIRED, ACCOUNT_LOCKED)</li>
 * </ul>
 *
 * @author Steve
 * @since 2025-10-15
 */
@Schema(description = "OTP 인증 결과 응답 DTO")
public class OtpResultDTO {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "결과 메시지", example = "OTP_VERIFIED")
    private String message;

    public OtpResultDTO() {}

    public OtpResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OtpResultDTO{" +
            "success=" + success +
            ", message='" + message + '\'' +
            '}';
    }
}
