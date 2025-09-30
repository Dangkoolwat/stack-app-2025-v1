package com.daangcool.stack.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Stack.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Logging logging = new Logging();

    public Logging getLogging() {
        return logging;
    }

    @Getter
    @Setter
    public static class Logging {
        private String filePath;
        private String maxFileSize;
        private int maxHistory;
        private String totalSizeCap;

    }


}
