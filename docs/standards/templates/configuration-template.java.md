```java
package com.example.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.app.config.properties.FeatureProperties;

/**
 * [Feature] Configuration
 *
 * 역할:
 * - [Feature] 관련 Bean 구성
 * - FeatureProperties 기반 설정 주입
 *
 * 에이전트 작업 가이드:
 * - Feature Bean 생성 또는 초기화 방식이 바뀔 때 수정한다.
 * - 관련 설정 변경 시 FeatureProperties와 application.yml도 함께 확인한다.
 *
 * 주의사항:
 * - 하드코딩된 값 사용 금지
 * - 설정 기본값과 런타임 동작이 어긋나지 않도록 주의
 *
 * 변경 이력:
 * - YYYY-MM-DD: [Task] Description
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FeatureProperties.class)
public class FeatureConfiguration {

    private final FeatureProperties properties;

    public FeatureConfiguration(FeatureProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ServiceType featureService() {
        log.info("Initializing feature with config: {}", properties);

        return new ServiceType(
            properties.getTimeoutSeconds()
        );
    }
}
```
