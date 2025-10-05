package com.daangcool.stack.config;

import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * This is a marker interface for test containers.
 */
public interface SqlTestContainer {
    JdbcDatabaseContainer<?> getTestContainer();
}
