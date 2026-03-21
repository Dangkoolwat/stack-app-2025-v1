package com.daangcool.stack.config;

import com.daangcool.stack.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ApplicationPropertiesTest {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Test
    void shouldLoadSecurityPublicPaths() {
        assertThat(applicationProperties.getSecurity().getPublicPaths().getStaticResources())
            .contains("/css/**", "/favicon.ico");
        assertThat(applicationProperties.getSecurity().getPublicPaths().getI18n())
            .contains("/i18n/*");
        assertThat(applicationProperties.getSecurity().getPublicPaths().getSwagger())
            .contains("/swagger-ui.html");
    }

    @Test
    void shouldLoadCacheTtl() {
        assertThat(applicationProperties.getCache().getTtl().getDefaultSeconds()).isEqualTo(3600);
        assertThat(applicationProperties.getCache().getTtl().getLongSeconds()).isEqualTo(86400);
        assertThat(applicationProperties.getCache().getTtl().getAuthSeconds()).isEqualTo(300);
    }
}
