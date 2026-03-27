package com.daangcool.stack.domain;

import com.daangcool.stack.common.constant.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.daangcool.stack.domain.vo.FileTypePolicy;
import com.daangcool.stack.domain.vo.FileUploadDefaults;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "stack_settings")
@Getter
@Setter
public class Settings extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Lob
    @Column(name = "global_settings")
    private String globalSettings;

    @Column(name = "description")
    private String description;

    @Transient
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * JSON 문자열을 Map으로 변환하여 반환합니다.
     */
    private Map<String, Object> getSettingsMap() {
        if (globalSettings == null || globalSettings.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(globalSettings, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("[SETTINGS ERROR] JSON Parse Failure: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Map을 JSON 문자열로 변환하여 저장합니다.
     */
    private void updateGlobalSettings(Map<String, Object> map) {
        try {
            this.globalSettings = mapper.writeValueAsString(map);
        } catch (Exception e) {
            // Log error
        }
    }

    // --- 개별 필드 접근용 델리게이트 메서드 ---

    public Long getTokenValiditySeconds() {
        Object value = getSettingsMap().get("tokenValiditySeconds");
        return value != null ? Long.valueOf(value.toString()) : 86400L;
    }

    public void setTokenValiditySeconds(Long value) {
        Map<String, Object> map = getSettingsMap();
        map.put("tokenValiditySeconds", value);
        updateGlobalSettings(map);
    }

    public Long getTokenValiditySecondsForRememberMe() {
        Object value = getSettingsMap().get("tokenValiditySecondsForRememberMe");
        return value != null ? Long.valueOf(value.toString()) : 2592000L;
    }

    public void setTokenValiditySecondsForRememberMe(Long value) {
        Map<String, Object> map = getSettingsMap();
        map.put("tokenValiditySecondsForRememberMe", value);
        updateGlobalSettings(map);
    }

    public int getLoginMaxFailureAttempts() {
        Object value = getSettingsMap().get("loginMaxFailureAttempts");
        return value != null ? Integer.parseInt(value.toString()) : Constants.MAX_ATTEMPT;
    }

    public void setLoginMaxFailureAttempts(int value) {
        Map<String, Object> map = getSettingsMap();
        map.put("loginMaxFailureAttempts", value);
        updateGlobalSettings(map);
    }

    /** 파일 업로드 전역 기본 설정 조회 */
    public FileUploadDefaults getFileUploadDefaults() {
        Object value = getSettingsMap().get("fileUploadDefaults");
        if (value == null) {
            return FileUploadDefaults.builder()
                .defaultMaxFileSizeBytes(10485760L) // 10MB
                .defaultMaxRequestSizeBytes(20971520L) // 20MB
                .blockUnmatched(true)
                .welcomeMessage("개별 정책이 없는 파일은 기본적으로 차단됩니다.")
                .build();
        }
        return mapper.convertValue(value, FileUploadDefaults.class);
    }

    /** 파일 업로드 전역 기본 설정 저장 */
    public void setFileUploadDefaults(FileUploadDefaults defaults) {
        Map<String, Object> map = getSettingsMap();
        map.put("fileUploadDefaults", defaults);
        updateGlobalSettings(map);
    }

    /** 파일 타입별 상세 정책 목록 조회 */
    public java.util.List<FileTypePolicy> getFileTypePolicies() {
        Object value = getSettingsMap().get("fileTypePolicies");
        if (value == null) {
            return java.util.Collections.emptyList();
        }
        return mapper.convertValue(value, new TypeReference<java.util.List<FileTypePolicy>>() {});
    }

    /** 파일 타입별 상세 정책 목록 저장 */
    public void setFileTypePolicies(java.util.List<FileTypePolicy> policies) {
        Map<String, Object> map = getSettingsMap();
        map.put("fileTypePolicies", policies);
        updateGlobalSettings(map);
    }

    /** 파일 업로드 추천 템플릿 목록 조회 */
    public java.util.List<FileTypePolicy> getFileTypeTemplates() {
        Object value = getSettingsMap().get("fileTypeTemplates");
        if (value == null) {
            return java.util.Collections.emptyList();
        }
        return mapper.convertValue(value, new TypeReference<java.util.List<FileTypePolicy>>() {});
    }

    /** 파일 업로드 추천 템플릿 목록 저장 */
    public void setFileTypeTemplates(java.util.List<FileTypePolicy> templates) {
        Map<String, Object> map = getSettingsMap();
        map.put("fileTypeTemplates", templates);
        updateGlobalSettings(map);
    }
}
