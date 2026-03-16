package com.daangcool.stack.web.rest.admin;

import com.daangcool.stack.service.RateLimitingManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자용 Rate Limiting 제어 API (W-1 고도화)
 * ------------------------------------------------------------------
 * 운영 중 특정 IP의 차단을 긴급하게 해제하거나, 정책 변경 후 상태를 초기화할 때 사용합니다.
 * 본 API는 Spring Security에 의해 ROLE_ADMIN 권한을 가진 사용자만 접근이 허용됩니다.
 * ------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/admin/rate-limit")
public class AdminRateLimitResource {

    private final Logger log = LoggerFactory.getLogger(AdminRateLimitResource.class);

    private final RateLimitingManagementService rateLimitingManagementService;

    /**
     * @param rateLimitingManagementService Rate Limiting 유지보수를 담당하는 서비스
     */
    public AdminRateLimitResource(RateLimitingManagementService rateLimitingManagementService) {
        this.rateLimitingManagementService = rateLimitingManagementService;
    }

    /**
     * {@code POST  /api/admin/rate-limit/clear} : 모든 Rate Limiting 버킷을 초기화합니다.
     * 분산 환경(Redis)의 경우 연결된 저장소의 모든 상태가 초기화됩니다.
     *
     * @return 성공 시 204 (No Content)
     */
    @PostMapping("/clear")
    public ResponseEntity<Void> clearRateLimits() {
        log.debug("REST request to clear all rate limiting buckets via Admin API");
        rateLimitingManagementService.clearAllBuckets();
        return ResponseEntity.noContent().build();
    }
}
