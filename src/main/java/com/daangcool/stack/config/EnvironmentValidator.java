package com.daangcool.stack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 운영 환경 필수 환경변수 검증
 * 
 * 🤖 에이전트 가이드:
 * - 운영(prod) 프로파일에서만 검증 실행
 * - JWT_SECRET, DATASOURCE 설정 누락 시 즉시 실패
 * - 새로운 필수 환경변수 추가 시 이 클래스에 검증 로직 추가
 */
@Slf4j
@Component
@Profile("prod")
public class EnvironmentValidator implements ApplicationRunner {
    
    @Value("${JWT_SECRET:}")
    private String jwtSecret;
    
    @Value("${SPRING_DATASOURCE_URL:}")
    private String datasourceUrl;
    
    @Value("${SPRING_DATASOURCE_USERNAME:}")
    private String datasourceUsername;
    
    @Value("${SPRING_DATASOURCE_PASSWORD:}")
    private String datasourcePassword;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("Running production environment validation...");
        
        // JWT Secret 검증
        Assert.hasText(jwtSecret, 
            "❌ JWT_SECRET environment variable must be set in production! " +
            "Generate with: openssl rand -base64 64");
        
        Assert.isTrue(jwtSecret.length() >= 64, 
            "❌ JWT_SECRET must be at least 64 characters long for security");
        
        // Database 검증
        Assert.hasText(datasourceUrl, 
            "❌ SPRING_DATASOURCE_URL must be set in production");
        
        Assert.hasText(datasourceUsername, 
            "❌ SPRING_DATASOURCE_USERNAME must be set in production");
        
        Assert.hasText(datasourcePassword, 
            "❌ SPRING_DATASOURCE_PASSWORD must be set in production");
        
        log.info("✅ Production environment validation passed safely.");
    }
}
