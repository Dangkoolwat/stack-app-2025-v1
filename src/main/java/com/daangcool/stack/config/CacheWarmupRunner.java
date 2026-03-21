package com.daangcool.stack.config;

import com.daangcool.stack.service.common.CommonCodeService;
import lombok.extern.slf4j.Slf4j;
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
    
    public CacheWarmupRunner(CommonCodeService commonCodeService) {
        this.commonCodeService = commonCodeService;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting Redis cache warm-up...");
        
        try {
            // 1. 공통 코드 그룹 리스트 워밍업
            commonCodeService.findAllGroups();
            
            // 2. 주요 공통 코드 상세 리스트 워밍업 ("COMMON" 등 핵심 그룹만)
            commonCodeService.findAllDetailsByGroup("COMMON");
            
            log.info("Redis cache warm-up completed successfully.");
        } catch (Exception e) {
            log.warn("Failed to warm-up Redis cache: {}", e.getMessage());
        }
    }
}
