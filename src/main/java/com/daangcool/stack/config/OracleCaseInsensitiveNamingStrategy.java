package com.daangcool.stack.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * 멀티 DB 이식성을 위한 커스텀 물리적 이름 지정 전략입니다.
 * <p>
 * - Oracle Dialect가 사용되는 경우, 모든 식별자(테이블, 컬럼 등)를 DB의 기본 규칙인 대문자(UPPERCASE)로 강제 변환합니다.
 * - 기타 DB(PostgreSQL 등)의 경우, 기본 규칙(소문자 스네이크 케이스)을 따릅니다.
 */
public class OracleCaseInsensitiveNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    // 프로젝트의 application-prod.yml 등에서 Oracle Dialect를 사용하고 있음을 확인 가능합니다.
    private static final String ORACLE_DIALECT_PREFIX = "org.hibernate.dialect.Oracle";

    // -------------------------------------------------------------------------
    // 1. 테이블 이름 변환 (Table Name)
    // -------------------------------------------------------------------------
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        // 기본 전략(CamelCase -> snake_case -> lowercase)을 먼저 수행
        Identifier defaultIdentifier = super.toPhysicalTableName(name, jdbcEnvironment);

        // Oracle Dialect가 사용되는 경우
        if (isOracleDialect(jdbcEnvironment)) {
            // 이름을 대문자로 강제 변환하고,
            // Oracle의 기본 동작을 따르도록 따옴표 강제 설정(isQuoted)을 해제합니다.
            return Identifier.toIdentifier(defaultIdentifier.getText().toUpperCase(Locale.ROOT), false);
        }

        // 그 외 DB의 경우, 기본 동작(소문자 스네이크 케이스)을 따름
        return defaultIdentifier;
    }

    // -------------------------------------------------------------------------
    // 2. 컬럼 이름 변환 (Column Name)
    // -------------------------------------------------------------------------
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        // 기본 전략(CamelCase -> snake_case -> lowercase)을 먼저 수행
        Identifier defaultIdentifier = super.toPhysicalColumnName(name, jdbcEnvironment);

        // Oracle Dialect가 사용되는 경우
        if (isOracleDialect(jdbcEnvironment)) {
            // 이름을 대문자로 강제 변환하고, 따옴표 강제 설정을 해제합니다.
            return Identifier.toIdentifier(defaultIdentifier.getText().toUpperCase(Locale.ROOT), false);
        }

        // 그 외 DB의 경우, 기본 동작을 따름
        return defaultIdentifier;
    }

    // -------------------------------------------------------------------------
    // 3. Dialect 체크 유틸리티
    // -------------------------------------------------------------------------
    /**
     * 현재 사용 중인 Hibernate Dialect이 Oracle 계열인지 확인합니다.
     */
    private boolean isOracleDialect(JdbcEnvironment jdbcEnvironment) {
        if (jdbcEnvironment == null || jdbcEnvironment.getDialect() == null) {
            return false;
        }
        String dialectClassName = jdbcEnvironment.getDialect().getClass().getName();
        // org.hibernate.dialect.OracleDialect 등으로 시작하는지 확인
        return dialectClassName.startsWith(ORACLE_DIALECT_PREFIX);
    }
}
