package com.daangcool.stack.service;

import com.daangcool.stack.config.Constants;
import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.config.JHipsterProperties;

import java.util.Optional;


@Service
@Transactional
public class GlobalSettingsService {

    private static final Long SETTINGS_ID = 1L; // Assuming a single row for settings

    private final SettingsRepository settingsRepository;
    private final JHipsterProperties jHipsterProperties;
    private final CacheManager cacheManager;

    public GlobalSettingsService(SettingsRepository settingsRepository, JHipsterProperties jHipsterProperties, CacheManager cacheManager) {
        this.settingsRepository = settingsRepository;
        this.jHipsterProperties = jHipsterProperties;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public SettingsDTO getSettings() {
        return settingsRepository.findById(SETTINGS_ID).map(settings -> new SettingsDTO(
            settings.getTokenValiditySeconds(),
            settings.getTokenValiditySecondsForRememberMe(),
            settings.getLoginMaxFailureAttempts(),
            settings.getDescription()
        )).orElse(new SettingsDTO(
            jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds(),
            jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe(),
            Constants.MAX_ATTEMPT, // Default max attempts
            "Default settings"
        ));
    }

    public void updateSettings(SettingsDTO settingsDTO) {
        Settings settings = settingsRepository.findById(SETTINGS_ID).orElseGet(() -> {
            Settings newSettings = new Settings();
            newSettings.setId(SETTINGS_ID);
            return newSettings;
        });

        settings.setTokenValiditySeconds(settingsDTO.getTokenValiditySeconds());
        settings.setTokenValiditySecondsForRememberMe(settingsDTO.getTokenValiditySecondsForRememberMe());
        settings.setLoginMaxFailureAttempts(settingsDTO.getLoginMaxFailureAttempts());
        settings.setDescription(settingsDTO.getDescription());

        settingsRepository.save(settings);
        clearSettingsCache();
    }

    private Optional<Settings> getSettingsFromRepository() {
        return settingsRepository.findById(SETTINGS_ID);
    }

    @Transactional(readOnly = true)
    public long getTokenValidityInSeconds() {
        return getSettingsFromRepository()
            .map(Settings::getTokenValiditySeconds)
            .orElse(jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds());
    }

    @Transactional(readOnly = true)
    public long getTokenValidityInSecondsForRememberMe() {
        return getSettingsFromRepository()
            .map(Settings::getTokenValiditySecondsForRememberMe)
            .orElse(jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe());
    }

    @Transactional(readOnly = true)
    public int getLoginMaxFailureAttempts() {
        return getSettingsFromRepository()
            .map(Settings::getLoginMaxFailureAttempts)
            .orElse(5); // Default value
    }

    private void clearSettingsCache() {
        Cache cache = cacheManager.getCache(Settings.class.getName());
        if (cache != null) {
            cache.evict(SETTINGS_ID);
        }
    }
}
