package com.daangcool.stack.config;

import com.daangcool.stack.service.GlobalSettingsService;
import com.daangcool.stack.service.common.CommonCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Redis Cache Warm-up Runner
 * 애플리케이션 시작 시 자주 사용하는 데이터(공통코드 등)를 미리 캐싱합니다.
 */
@Slf4j
@Component
public class CacheWarmupRunner implements ApplicationRunner {
    
    private final CommonCodeService commonCodeService;
    private final GlobalSettingsService globalSettingsService;

    @Value("${spring.liquibase.drop-first:false}")
    private boolean isLiquibaseDropFirst;

    public CacheWarmupRunner(CommonCodeService commonCodeService, GlobalSettingsService globalSettingsService) {
        this.commonCodeService = commonCodeService;
        this.globalSettingsService = globalSettingsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isLiquibaseDropFirst) {
            log.info("Liquibase drop-first is enabled. Skipping cache warm-up to avoid initialization conflicts.");
            return;
        }

        log.info("Starting Redis cache warm-up...");

        try {
            // 1. 공통 코드 그룹 리스트 워밍업
            commonCodeService.findAllGroups();

            // 2. 주요 공통 코드 상세 리스트 워밍업 ("COMMON" 등 핵심 그룹만)
            commonCodeService.findAllDetailsByGroup("COMMON");

            // 3. 글로벌 설정 워밍업
            try {
                globalSettingsService.getSettings();
            } catch (Exception e) {
                log.info("Global settings not ready during startup: {}. Skipping settings warm-up.", e.getMessage());
            }

            log.info("Redis cache warm-up completed successfully.");
        } catch (Exception e) {
            log.warn("Failed to warm-up Redis cache: {}", e.getMessage());
        }
    }
}
