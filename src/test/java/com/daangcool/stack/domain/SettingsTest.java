package com.daangcool.stack.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SettingsTest {

    @Test
    void testJsonParsingAndDelegation() {
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setGlobalSettings("{\"tokenValiditySeconds\": 123}");
        settings.setDescription("Test Description");
        
        assertThat(settings.getTokenValiditySeconds()).isEqualTo(123L);
        assertThat(settings.getDescription()).isEqualTo("Test Description");
        
        settings.setLoginMaxFailureAttempts(10);
        assertThat(settings.getGlobalSettings()).contains("\"loginMaxFailureAttempts\":10");
        assertThat(settings.getGlobalSettings()).doesNotContain("description");
        assertThat(settings.getLoginMaxFailureAttempts()).isEqualTo(10);
    }

    @Test
    void testDefaultValuesOnEmptyJson() {
        Settings settings = new Settings();
        settings.setGlobalSettings("");
        
        // 86400 is the fallback value in the delegate method
        assertThat(settings.getTokenValiditySeconds()).isEqualTo(86400L);
        // 5 is the default value (Constants.MAX_ATTEMPT)
        assertThat(settings.getLoginMaxFailureAttempts()).isEqualTo(5);
    }
}
