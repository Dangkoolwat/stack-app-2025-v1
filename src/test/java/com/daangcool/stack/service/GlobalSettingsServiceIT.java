package com.daangcool.stack.service;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@Transactional
class GlobalSettingsServiceIT {

    @Autowired
    private GlobalSettingsService globalSettingsService;

    @Autowired
    private SettingsRepository settingsRepository;

    @Test
    void testGetAndUpdateSettings() {
        Settings settings = settingsRepository.findById(1L).orElseGet(() -> {
            Settings newSettings = new Settings();
            newSettings.setId(1L);
            return newSettings;
        });
        
        settings.setGlobalSettings("{\"tokenValiditySeconds\": 3600, \"tokenValiditySecondsForRememberMe\": 7200, \"loginMaxFailureAttempts\": 5}");
        settings.setDescription("Integration Test Column");
        settingsRepository.saveAndFlush(settings);

        // Service를 통해 조회
        SettingsDTO dto = globalSettingsService.getSettings();
        assertThat(dto.getTokenValiditySeconds()).isEqualTo(3600L);
        assertThat(dto.getDescription()).isEqualTo("Integration Test Column");

        // Service를 통해 업데이트
        dto.setTokenValiditySeconds(7200L);
        dto.setDescription("Updated by service");
        globalSettingsService.updateSettings(dto);

        // DB에 직접 반영되었는지 확인
        Settings updated = settingsRepository.findById(1L).orElseThrow();
        assertThat(updated.getTokenValiditySeconds()).isEqualTo(7200L);
        assertThat(updated.getDescription()).isEqualTo("Updated by service");
        assertThat(updated.getGlobalSettings()).doesNotContain("description");
    }
}
