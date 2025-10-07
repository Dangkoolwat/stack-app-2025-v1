package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link SettingsResource} REST controller.
 */
@AutoConfigureMockMvc
@IntegrationTest
@Transactional
class SettingsResourceIT {

    @Autowired
    private MockMvc restSettingsMockMvc;

    @Autowired
    private SettingsRepository settingsRepository;

    private static final Long UPDATED_TOKEN_VALIDITY_SECONDS = 3600L;
    private static final Long UPDATED_REMEMBER_ME_SECONDS = 1209600L;
    private static final int UPDATED_MAX_ATTEMPTS = 10;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getSettings_asAdmin_shouldReturnDefaultSettingsFromDb() throws Exception {
        // As Liquibase has already inserted the initial data, this test verifies it.
        restSettingsMockMvc.perform(get("/api/settings"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.tokenValiditySeconds").value(86400L))
            .andExpect(jsonPath("$.tokenValiditySecondsForRememberMe").value(2592000L))
            .andExpect(jsonPath("$.loginMaxFailureAttempts").value(5));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateSettings_asAdmin_shouldUpdateSettings() throws Exception {
        // Initial state check
        long initialCount = settingsRepository.count();
        assertThat(initialCount).isEqualTo(1);

        SettingsDTO settingsDTO = new SettingsDTO(
            UPDATED_TOKEN_VALIDITY_SECONDS,
            UPDATED_REMEMBER_ME_SECONDS,
            UPDATED_MAX_ATTEMPTS,
            "Updated settings"
        );

        restSettingsMockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(settingsDTO)))
            .andExpect(status().isOk());

        // Validate the changes in the database
        Settings updatedSettings = settingsRepository.findById(1L).orElseThrow();
        assertThat(updatedSettings.getTokenValiditySeconds()).isEqualTo(UPDATED_TOKEN_VALIDITY_SECONDS);
        assertThat(updatedSettings.getTokenValiditySecondsForRememberMe()).isEqualTo(UPDATED_REMEMBER_ME_SECONDS);
        assertThat(updatedSettings.getLoginMaxFailureAttempts()).isEqualTo(UPDATED_MAX_ATTEMPTS);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getSettings_asUser_shouldBeForbidden() throws Exception {
        restSettingsMockMvc.perform(get("/api/settings"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getSettings_unauthenticated_shouldBeUnauthorized() throws Exception {
        restSettingsMockMvc.perform(get("/api/settings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void updateSettings_asUser_shouldBeForbidden() throws Exception {
        SettingsDTO settingsDTO = new SettingsDTO(1800L, 2592000L, 5, "");

        restSettingsMockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(settingsDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateSettings_unauthenticated_shouldBeUnauthorized() throws Exception {
        SettingsDTO settingsDTO = new SettingsDTO(1800L, 2592000L, 5, "");

        restSettingsMockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(settingsDTO)))
            .andExpect(status().isUnauthorized());
    }
}
