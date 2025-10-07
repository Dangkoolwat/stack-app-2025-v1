package com.daangcool.stack.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainer implements SqlTestContainer, InitializingBean, DisposableBean {

    private final Logger log = LoggerFactory.getLogger(TestContainer.class);
    private OracleContainer oracleContainer;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == oracleContainer) {
            oracleContainer = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
                .withDatabaseName("ORCLTEST")
                .withPassword("oracle")
                .withEnv("ORACLE_PASSWORD", "oracle")
                .withReuse(true)
                .withStartupTimeoutSeconds(300);
            oracleContainer.start();
            log.info("Started Oracle Testcontainer with URL: {}", oracleContainer.getJdbcUrl());
        }
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return oracleContainer;
    }

    @Override
    public void destroy() throws Exception {
        if (null != oracleContainer && oracleContainer.isRunning()) {
            oracleContainer.stop();
            log.info("Stopped Oracle Testcontainer");
        }
    }
}
