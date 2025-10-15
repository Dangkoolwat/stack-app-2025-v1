package com.daangcool.stack.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * OTP 로그인 성공 시 JWT 토큰 및 사용자 정보 반환 DTO.
 */
@Getter
@Setter
@Schema(description = "OTP 로그인 성공 응답 DTO")
public class OtpLoginResponseDTO {

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String jwt;

    @Schema(description = "로그인 ID", example = "user@example.com")
    private String login;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "Steve")
    private String name;

    public OtpLoginResponseDTO() {}

    public OtpLoginResponseDTO(String jwt, String login, String email, String name) {
        this.jwt = jwt;
        this.login = login;
        this.email = email;
        this.name = name;
    }


}

