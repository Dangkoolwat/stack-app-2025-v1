package com.daangcool.stack.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * 멀티 DB 이식성을 위한 커스텀 물리적 이름 지정 전략입니다.
 * <p>
 * - Hibernate 6/7 및 Spring Boot 4 호환을 위해 PhysicalNamingStrategyStandardImpl을 상속합니다.
 */
public class OracleCaseInsensitiveNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    private static final String ORACLE_DIALECT_PREFIX = "org.hibernate.dialect.Oracle";

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return formatIdentifier(super.toPhysicalTableName(name, jdbcEnvironment), jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return formatIdentifier(super.toPhysicalColumnName(name, jdbcEnvironment), jdbcEnvironment);
    }

    /**
     * 공통 변환 로직: CamelCase -> snake_case 변환 후 Dialect에 따른 대소문자 처리
     */
    private Identifier formatIdentifier(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        if (identifier == null) return null;

        // 1. CamelCase를 snake_case로 변환 (기존 상속받았던 전략의 핵심 로직 직접 구현)
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        String newName = identifier.getText().replaceAll(regex, replacement).toLowerCase(Locale.ROOT);

        // 2. Oracle Dialect 처리
        if (isOracleDialect(jdbcEnvironment)) {
            return Identifier.toIdentifier(newName.toUpperCase(Locale.ROOT), false);
        }

        // 3. 기타 DB 처리 (기본 소문자 스네이크 케이스)
        return Identifier.toIdentifier(newName, false);
    }

    private boolean isOracleDialect(JdbcEnvironment jdbcEnvironment) {
        if (jdbcEnvironment == null || jdbcEnvironment.getDialect() == null) {
            return false;
        }
        String dialectClassName = jdbcEnvironment.getDialect().getClass().getName();
        return dialectClassName.startsWith(ORACLE_DIALECT_PREFIX);
    }
}
