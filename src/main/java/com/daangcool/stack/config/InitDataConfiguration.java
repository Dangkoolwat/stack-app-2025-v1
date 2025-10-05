package com.daangcool.stack.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("data")
public class InitDataConfiguration {

    private final Logger log = LoggerFactory.getLogger(InitDataConfiguration.class);

    @PostConstruct
    public void initiateData() {
        log.debug("Skipping data initialization in Java configuration, as it is now handled by Liquibase.");
        // Data initialization is now handled by Liquibase in 00000000000000_initial_schema.xml
    }
}
