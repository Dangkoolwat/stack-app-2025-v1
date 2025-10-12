package com.daangcool.stack.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tech.jhipster.config.JHipsterProperties;

@ExtendWith(MockitoExtension.class)
class GlobalSettingsServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private JHipsterProperties jHipsterProperties;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private GlobalSettingsService globalSettingsService;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(GlobalSettingsService.SETTING_CACHE)).thenReturn(cache);
    }

    @Test
    void getSettingsShouldReturnCachedValueWhenAvailable() {
        SettingsDTO cached = new SettingsDTO(10L, 20L, 3, "cached");
        when(cache.get(eq(1L), eq(SettingsDTO.class))).thenReturn(cached);

        SettingsDTO result = globalSettingsService.getSettings();

        assertThat(result).isSameAs(cached);
        verify(settingsRepository, never()).findById(anyLong());
        verify(cache, never()).put(any(), any());
    }

    @Test
    void getSettingsShouldLoadFromRepositoryAndPopulateCacheWhenMissing() {
        when(cache.get(eq(1L), eq(SettingsDTO.class))).thenReturn(null);
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setTokenValiditySeconds(30L);
        settings.setTokenValiditySecondsForRememberMe(60L);
        settings.setLoginMaxFailureAttempts(5);
        settings.setDescription("from-db");
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        SettingsDTO result = globalSettingsService.getSettings();

        assertThat(result.getTokenValiditySeconds()).isEqualTo(30L);
        assertThat(result.getTokenValiditySecondsForRememberMe()).isEqualTo(60L);
        assertThat(result.getLoginMaxFailureAttempts()).isEqualTo(5);
        assertThat(result.getDescription()).isEqualTo("from-db");
        verify(cache).put(1L, result);
    }

    @Test
    void updateSettingsShouldThrowWhenTokenValidityIsNonPositive() {
        Settings persisted = new Settings();
        persisted.setId(1L);
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(persisted));
        SettingsDTO invalid = new SettingsDTO(0L, 10L, 3, "invalid");

        assertThatThrownBy(() -> globalSettingsService.updateSettings(invalid)).isInstanceOf(BadRequestAlertException.class);

        verify(settingsRepository, never()).save(any());
        verify(cache, never()).evict(any());
    }

    @Test
    void updateSettingsShouldPersistChangesAndEvictCache() {
        Settings persisted = new Settings();
        persisted.setId(1L);
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(persisted));
        SettingsDTO dto = new SettingsDTO(120L, 240L, 7, "updated");

        globalSettingsService.updateSettings(dto);

        assertThat(persisted.getTokenValiditySeconds()).isEqualTo(120L);
        assertThat(persisted.getTokenValiditySecondsForRememberMe()).isEqualTo(240L);
        assertThat(persisted.getLoginMaxFailureAttempts()).isEqualTo(7);
        assertThat(persisted.getDescription()).isEqualTo("updated");
        verify(settingsRepository).save(persisted);
        verify(cache).evict(1L);
        verifyNoMoreInteractions(cache);
    }
}
