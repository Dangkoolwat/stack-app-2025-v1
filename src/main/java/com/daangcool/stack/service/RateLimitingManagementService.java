package com.daangcool.stack.service;

import com.daangcool.stack.security.RateLimitingRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Rate Limiting 관리 및 유지보수 서비스 (W-1 고도화)
 * ------------------------------------------------------------------
 * 자동 스케줄링을 통한 리소스 정리 및 인스턴스 간 수동 제어 로직을 담당합니다.
 *
 * 주요 기능:
 * - 매일 새벽 시간대 버킷 리소스 정리 (Memory Leak 방지)
 * - 관리자 요청 시 모든 차단 상태 즉시 해제
 * ------------------------------------------------------------------
 */
@Service
public class RateLimitingManagementService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingManagementService.class);

    private final RateLimitingRegistry registry;

    /**
     * @param registry 버킷 상태가 저장된 레지스트리
     */
    public RateLimitingManagementService(RateLimitingRegistry registry) {
        this.registry = registry;
    }

    /**
     * 정기적인 버킷 클린업 수행 (자동 스케줄링)
     * 매일 새벽 3시에 실행되어 활동이 없는 토큰 버킷 정보를 정리합니다.
     * (분산 환경의 경우 Redis의 TTL 정책에 의해 보조되지만, 애플리케이션 레벨의 일관성을 위해 유지합니다.)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledCleanup() {
        log.info("Starting scheduled rate limiting bucket cleanup task...");
        registry.clear();
    }

    /**
     * 모든 Rate Limiting 상태를 즉시 초기화합니다.
     * 관리자 도구 등에서 모든 차단을 실시간으로 해제해야 할 때 호출됩니다.
     */
    public void clearAllBuckets() {
        log.info("Request received to manually clear all rate limiting status across the system.");
        registry.clear();
    }
}
