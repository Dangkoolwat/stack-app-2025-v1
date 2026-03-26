package com.daangcool.stack.web.rest;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.security.jwt.JwtAuthenticationTestUtils;
import org.springframework.http.HttpHeaders;
import java.util.Collections;
import java.util.List;
import tools.jackson.databind.ObjectMapper;

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

    @Autowired
    private ObjectMapper om;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    private String createToken(String login) {
        return JwtAuthenticationTestUtils.createValidTokenForUser(jwtKey, login);
    }

    private String createTokenWithAuthorities(String login, List<String> authorities) {
        return JwtAuthenticationTestUtils.createTokenForUser(jwtKey, login, authorities);
    }

    private static final Long UPDATED_TOKEN_VALIDITY_SECONDS = 3600L;
    private static final Long UPDATED_REMEMBER_ME_SECONDS = 1209600L;
    private static final int UPDATED_MAX_ATTEMPTS = 10;

    @Test
    void getSettings_asAdmin_shouldReturnDefaultSettingsFromDb() throws Exception {


        restSettingsMockMvc.perform(get("/api/settings")
                .header(HttpHeaders.AUTHORIZATION, JwtAuthenticationTestUtils.BEARER + createToken("admin")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.tokenValiditySeconds").value(86400L))
            .andExpect(jsonPath("$.loginMaxFailureAttempts").value(5))
            .andExpect(jsonPath("$.fileTypeTemplates").isArray())
            .andExpect(jsonPath("$.fileTypeTemplates[0].key").value("image-standard"));
    }

    @Test
    void updateSettings_asAdmin_shouldUpdateSettings() throws Exception {
        // Initial state check
        long initialCount = settingsRepository.count();
        assertThat(initialCount).isEqualTo(1);

        SettingsDTO settingsDTO = new SettingsDTO(
            UPDATED_TOKEN_VALIDITY_SECONDS,
            UPDATED_REMEMBER_ME_SECONDS,
            UPDATED_MAX_ATTEMPTS,
            "Updated settings",
            null,
            null,
            null
        );

        restSettingsMockMvc.perform(put("/api/settings")
                .header(HttpHeaders.AUTHORIZATION, JwtAuthenticationTestUtils.BEARER + createToken("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingsDTO)))
            .andExpect(status().isOk());

        // Validate the changes in the database
        Settings updatedSettings = settingsRepository.findById(1L).orElseThrow();
        assertThat(updatedSettings.getTokenValiditySeconds()).isEqualTo(UPDATED_TOKEN_VALIDITY_SECONDS);
        assertThat(updatedSettings.getTokenValiditySecondsForRememberMe()).isEqualTo(UPDATED_REMEMBER_ME_SECONDS);
        assertThat(updatedSettings.getLoginMaxFailureAttempts()).isEqualTo(UPDATED_MAX_ATTEMPTS);
    }

    @Test
    void getSettings_asUser_shouldBeForbidden() throws Exception {
        restSettingsMockMvc.perform(get("/api/settings")
                .header(HttpHeaders.AUTHORIZATION, JwtAuthenticationTestUtils.BEARER + 
                    createTokenWithAuthorities("user", Collections.singletonList(AuthoritiesConstants.USER))))
            .andExpect(status().isForbidden());
    }

    @Test
    void getSettings_unauthenticated_shouldBeUnauthorized() throws Exception {
        restSettingsMockMvc.perform(get("/api/settings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSettings_asUser_shouldBeForbidden() throws Exception {
        SettingsDTO settingsDTO = new SettingsDTO(1800L, 2592000L, 5, "", null, null, null);

        restSettingsMockMvc.perform(put("/api/settings")
                .header(HttpHeaders.AUTHORIZATION, JwtAuthenticationTestUtils.BEARER + 
                    createTokenWithAuthorities("user", Collections.singletonList(AuthoritiesConstants.USER)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingsDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    void updateSettings_unauthenticated_shouldBeUnauthorized() throws Exception {
        SettingsDTO settingsDTO = new SettingsDTO(1800L, 2592000L, 5, "", null, null, null);

        restSettingsMockMvc.perform(put("/api/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsBytes(settingsDTO)))
            .andExpect(status().isUnauthorized());
    }
}
