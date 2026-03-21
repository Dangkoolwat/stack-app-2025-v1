```java
package com.example.app.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * [Feature] 설정
 *
 * 역할:
 * - application.yml의 feature 설정 바인딩
 * - 런타임 검증 가능한 설정 구조 제공
 *
 * 에이전트 작업 가이드:
 * - 새로운 기능 설정을 추가하거나 하드코딩 값을 외부화할 때 수정한다.
 * - application.yml prefix와 환경변수 이름을 함께 맞춘다.
 *
 * 주의사항:
 * - 모든 값은 환경변수 placeholder 기반으로 정의되어야 한다.
 * - validation 없는 설정 추가를 지양한다.
 *
 * 변경 이력:
 * - YYYY-MM-DD: [Task] Description
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application.feature-name")
public class FeatureProperties {

    /**
     * 기능 활성화 여부
     * env: FEATURE_ENABLED
     * default: false
     */
    @NotNull
    private Boolean enabled = false;

    /**
     * 타임아웃 (초)
     * env: FEATURE_TIMEOUT_SECONDS
     * default: 30
     */
    @Min(1)
    @Max(300)
    private Integer timeoutSeconds = 30;
}
```
