package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.vo.FileTypePolicy;
import com.daangcool.stack.domain.vo.FileUploadDefaults;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.Settings} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long tokenValiditySeconds;

    @NotNull
    private Long tokenValiditySecondsForRememberMe;

    @NotNull
    private int loginMaxFailureAttempts;

    private String description;

    /** 파일 업로드 전역 기본 설정 */
    private FileUploadDefaults fileUploadDefaults;

    /** 파일 타입별 상세 정책 목록 */
    private java.util.List<FileTypePolicy> fileTypePolicies;

    /** 파일 정책 추천 템플릿 목록 */
    private java.util.List<FileTypePolicy> fileTypeTemplates;
}
