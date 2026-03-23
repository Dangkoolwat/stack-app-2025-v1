package com.daangcool.stack.domain;

import com.daangcool.stack.common.constant.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Settings extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "global_settings", length = 2000)
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
}
