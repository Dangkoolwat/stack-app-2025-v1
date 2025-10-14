package com.daangcool.stack.service;

import com.daangcool.stack.domain.Settings;
import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import com.daangcool.stack.web.exception.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tech.jhipster.config.JHipsterProperties;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 단위 테스트: GlobalSettingsService
 */
@ExtendWith(MockitoExtension.class)
class GlobalSettingsServiceT {

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private JHipsterProperties jHipsterProperties; // 현재 테스트에서는 사용되지 않지만, 의존성이므로 Mock으로 생성

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private GlobalSettingsService globalSettingsService;

    private Settings settings;
    private SettingsDTO settingsDTO;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 공통 객체들을 초기화합니다.
        settings = new Settings();
        settings.setId(1L);
        settings.setTokenValiditySeconds(3600L);
        settings.setTokenValiditySecondsForRememberMe(7200L);
        settings.setLoginMaxFailureAttempts(5);

        settingsDTO = new SettingsDTO(
            settings.getTokenValiditySeconds(),
            settings.getTokenValiditySecondsForRememberMe(),
            settings.getLoginMaxFailureAttempts(),
            "Description"
        );

        // 캐시 관련 Mock 설정
        lenient().when(cacheManager.getCache(GlobalSettingsService.SETTING_CACHE)).thenReturn(cache);
    }

    /**
     * 설정 조회 테스트 (Cache Miss 시나리오)
     * - 캐시에 데이터가 없을 때, DB에서 조회하고 캐시에 저장하는지 확인합니다.
     */
    @Test
    void getSettings_WhenCacheMiss_ShouldLoadFromDbAndCache() {
        // given
        when(cache.get(anyLong(), eq(SettingsDTO.class))).thenReturn(null); // 캐시 miss
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        // when
        SettingsDTO result = globalSettingsService.getSettings();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTokenValiditySeconds()).isEqualTo(3600L);

        // DB 조회와 캐시 저장이 각각 1번씩 호출되었는지 검증합니다.
        verify(settingsRepository, times(1)).findById(1L);
        verify(cache, times(1)).put(1L, result);
    }

    /**
     * 설정 조회 테스트 (Cache Hit 시나리오)
     * - 캐시에 데이터가 있을 때, DB를 조회하지 않고 캐시에서 바로 반환하는지 확인합니다.
     */
    @Test
    void getSettings_WhenCacheHit_ShouldReturnCachedData() {
        // given
        when(cache.get(anyLong(), eq(SettingsDTO.class))).thenReturn(settingsDTO); // 캐시 hit

        // when
        SettingsDTO result = globalSettingsService.getSettings();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTokenValiditySeconds()).isEqualTo(settingsDTO.getTokenValiditySeconds());

        // DB 조회가 발생하지 않았는지 검증합니다.
        verify(settingsRepository, never()).findById(anyLong());
    }

    /**
     * 설정 업데이트 테스트 (성공 케이스)
     * - 설정 업데이트 후, DB에 저장되고 캐시가 무효화되는지 확인합니다.
     */
    @Test
    void updateSettings_ValidData_ShouldUpdateDbAndClearCache() {
        // given
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        SettingsDTO updateRequest = new SettingsDTO(86400L, 2592000L, 10, "Updated");

        // when
        globalSettingsService.updateSettings(updateRequest);

        // then
        // save 메소드가 호출되었는지 검증합니다.
        verify(settingsRepository, times(1)).save(any(Settings.class));
        // 캐시의 evict 메소드가 호출되었는지 검증합니다.
        verify(cache, times(1)).evict(1L);

        // 실제 객체의 값이 변경되었는지 확인합니다.
        assertThat(settings.getTokenValiditySeconds()).isEqualTo(86400L);
        assertThat(settings.getLoginMaxFailureAttempts()).isEqualTo(10);
    }

    /**
     * 설정 업데이트 테스트 (실패 케이스 - 유효하지 않은 데이터)
     * - 유효하지 않은 값으로 업데이트 시도 시, 예외가 발생하는지 확인합니다.
     */
    @Test
    void updateSettings_InvalidData_ShouldThrowException() {
        // given
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));
        SettingsDTO invalidDTO = new SettingsDTO(0L, 7200L, 5, "Invalid");

        // when & then
        assertThatThrownBy(() -> globalSettingsService.updateSettings(invalidDTO))
            .isInstanceOf(BadRequestAlertException.class)
            .hasMessageContaining("Token validity must be greater than zero");
    }
}
