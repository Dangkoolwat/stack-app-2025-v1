package com.daangcool.stack.service;

import com.daangcool.stack.common.constant.Constants;
import com.daangcool.stack.domain.Settings;

import com.daangcool.stack.repository.SettingsRepository;
import com.daangcool.stack.service.dto.SettingsDTO;
import com.daangcool.stack.common.exception.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.config.JHipsterProperties;

/**
 * GlobalSettingsService
 *
 * 시스템 전역 설정(Global Settings)을 관리하는 서비스 클래스입니다.
 *
 * 이 서비스는 오직 하나의 Settings 엔티티(ID=1)를 기반으로 동작합니다.
 * - 조회 시 캐시를 먼저 확인합니다.
 * - 캐시에 데이터가 없으면 DB에서 조회 후 캐시에 저장합니다.
 * - 수정 시 캐시를 즉시 무효화하여 일관성을 유지합니다.
 *
 * 캐시 저장소는 CacheConfiguration에서 지정된 Redis 기반 JCache를 사용합니다.
 */
@Service
@Transactional
public class GlobalSettingsService {

    private static final Logger log = LoggerFactory.getLogger(GlobalSettingsService.class);

    // Settings는 항상 단일 레코드로 존재 (ID=1)
    private static final Long SETTINGS_ID = 1L;

    // CacheConfiguration에 등록된 캐시 이름과 동일해야 함
    public static final String SETTING_CACHE = "SETTING_CACHE";

    private final SettingsRepository settingsRepository;
    private final JHipsterProperties jHipsterProperties;
    private final CacheManager cacheManager;

    public GlobalSettingsService(SettingsRepository settingsRepository, JHipsterProperties jHipsterProperties, CacheManager cacheManager) {
        this.settingsRepository = settingsRepository;
        this.jHipsterProperties = jHipsterProperties;
        this.cacheManager = cacheManager;
    }

    /**
     * 시스템 설정 조회
     *
     * 1. 캐시에서 먼저 설정 정보를 조회합니다.
     * 2. 캐시에 없으면 DB에서 조회한 뒤 캐시에 저장합니다.
     * 3. Settings 엔티티가 존재하지 않으면 예외를 던집니다.
     */
    @Transactional(readOnly = true)
    public SettingsDTO getSettings() {
        Cache cache = cacheManager.getCache(SETTING_CACHE);
        if (cache != null) {
            SettingsDTO cached = cache.get(SETTINGS_ID, SettingsDTO.class);
            if (cached != null) {
                log.trace("[SETTINGS CACHE] Cache hit for settings ID={}", SETTINGS_ID);
                return cached;
            }
        }

        log.debug("[SETTINGS CACHE] Cache miss. Loading from database...");
        Settings settings = settingsRepository.findById(SETTINGS_ID)
            .orElseThrow(() -> new IllegalStateException("Settings with ID=1 not found. The database schema may be inconsistent."));

        SettingsDTO dto = new SettingsDTO(
            settings.getTokenValiditySeconds(),
            settings.getTokenValiditySecondsForRememberMe(),
            settings.getLoginMaxFailureAttempts(),
            settings.getDescription(),
            settings.getFileUploadDefaults(),
            settings.getFileTypePolicies(),
            settings.getFileTypeTemplates()
        );

        if (cache != null) {
            cache.put(SETTINGS_ID, dto);
            log.debug("[SETTINGS CACHE] Cached settings for ID={}", SETTINGS_ID);
        }

        return dto;
    }

    /**
     * 시스템 설정 업데이트
     *
     * - DB에 설정 값을 저장합니다.
     * - 변경 후 캐시를 반드시 무효화합니다.
     * - 관리자가 시스템 정책을 변경할 때 사용하는 메서드입니다.
     */
    public void updateSettings(SettingsDTO settingsDTO) {
        Settings settings = settingsRepository.findById(SETTINGS_ID)
            .orElseThrow(() -> new IllegalStateException("Settings with ID=1 not found. Cannot update non-existent settings."));

        // 유효성 검사
        if (settingsDTO.getTokenValiditySeconds() <= 0 || settingsDTO.getTokenValiditySecondsForRememberMe() <= 0) {
            throw new BadRequestAlertException("Token validity must be greater than zero", "settings", "invalid.token");
        }

        // 설정값 갱신
        settings.setTokenValiditySeconds(settingsDTO.getTokenValiditySeconds());
        settings.setTokenValiditySecondsForRememberMe(settingsDTO.getTokenValiditySecondsForRememberMe());
        settings.setLoginMaxFailureAttempts(settingsDTO.getLoginMaxFailureAttempts());
        settings.setDescription(settingsDTO.getDescription());
        settings.setFileUploadDefaults(settingsDTO.getFileUploadDefaults());
        settings.setFileTypePolicies(settingsDTO.getFileTypePolicies());
        settings.setFileTypeTemplates(settingsDTO.getFileTypeTemplates());

        // DB 저장
        settingsRepository.save(settings);

        // 캐시 무효화 (일관성 유지)
        clearSettingsCache();
        log.info("[SETTINGS CACHE] Updated settings and cleared cache");
    }

    /**
     * JWT 토큰 유효시간(일반 로그인용) 조회
     *
     * - 캐시에 상관없이 항상 최신 DB 값을 조회합니다.
     * - 설정이 존재하지 않으면 기본값(JHipsterProperties)로 대체합니다.
     */
    @Transactional(readOnly = true)
    public long getTokenValidityInSeconds() {
        return settingsRepository.findById(SETTINGS_ID)
            .map(Settings::getTokenValiditySeconds)
            .orElse(jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds());
    }

    /**
     * JWT 토큰 유효시간(자동 로그인용 Remember-Me) 조회
     *
     * - 캐시에 상관없이 항상 최신 DB 값을 조회합니다.
     * - 설정이 존재하지 않으면 기본값(JHipsterProperties)로 대체합니다.
     */
    @Transactional(readOnly = true)
    public long getTokenValidityInSecondsForRememberMe() {
        return settingsRepository.findById(SETTINGS_ID)
            .map(Settings::getTokenValiditySecondsForRememberMe)
            .orElse(jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe());
    }

    /**
     * 로그인 실패 허용 횟수 조회
     *
     * - 캐시에 상관없이 항상 최신 DB 값을 조회합니다.
     * - 설정이 존재하지 않으면 시스템 상수(Constants.MAX_ATTEMPT)를 기본값으로 사용합니다.
     */
    @Transactional(readOnly = true)
    public int getLoginMaxFailureAttempts() {
        return settingsRepository.findById(SETTINGS_ID)
            .map(Settings::getLoginMaxFailureAttempts)
            .orElse(Constants.MAX_ATTEMPT);
    }

    /**
     * 캐시 초기화
     *
     * - 관리자나 운영자가 시스템 설정 변경 후 수동으로 호출할 수 있습니다.
     * - 잘못된 캐시 데이터나 긴급 수정 상황에 대비하기 위한 보조 메서드입니다.
     */
    public void clearSettingsCache() {
        Cache cache = cacheManager.getCache(SETTING_CACHE);
        if (cache != null) {
            log.info("[SETTINGS CACHE] Evicting cache for ID={}", SETTINGS_ID);
            cache.evict(SETTINGS_ID);
        }
    }
}
