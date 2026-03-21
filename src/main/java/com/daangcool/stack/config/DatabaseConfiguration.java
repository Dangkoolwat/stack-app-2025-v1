package com.daangcool.stack.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * 데이터베이스 설정 및 HikariCP DataSource 빈 정의.
 *
 * - maximumPoolSize: application.yml 에서 설정값이 있으면 사용하고, 없으면 CPU 코어 수 기반 자동 계산.
 *   자동 계산 로직: (코어 수 * 2) + 1
 * - 기타 HikariCP 설정은 ApplicationProperties.Database 에서 외부화 가능.
 */
@Configuration
@EnableJpaRepositories({ "com.daangcool.stack.repository" })
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableTransactionManagement
public class DatabaseConfiguration {

    /**
     * HikariDataSource 빈을 생성합니다.
     *
     * @param env Spring Environment (DB 연결 프로퍼티 조회용)
     * @param props ApplicationProperties 빈 (Spring이 자동 주입)
     * @return HikariDataSource 인스턴스
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(Environment env, ApplicationProperties props) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        
        String driver = env.getProperty("spring.datasource.driver-class-name");
        if (driver != null) {
            dataSource.setDriverClassName(driver);
        }

        // CPU 코어 기반 자동 풀 사이즈 계산
        int cores = Runtime.getRuntime().availableProcessors();
        int calculatedPoolSize = (cores * 2) + 1;

        ApplicationProperties.Database dbProps = props.getDatabase();
        
        // 최대 풀 사이즈: 설정값이 0 이하이면 자동 계산값 사용
        if (dbProps.getMaxPoolSize() > 0) {
            dataSource.setMaximumPoolSize(dbProps.getMaxPoolSize());
        } else {
            dataSource.setMaximumPoolSize(calculatedPoolSize);
        }

        if (dbProps.getMinimumIdle() < 0) {
            dataSource.setMinimumIdle(dataSource.getMaximumPoolSize());
        } else {
            dataSource.setMinimumIdle(dbProps.getMinimumIdle());
        }
        
        dataSource.setConnectionTimeout(dbProps.getConnectionTimeout());
        dataSource.setIdleTimeout(dbProps.getIdleTimeout());
        dataSource.setMaxLifetime(dbProps.getMaxLifetime());
        dataSource.setKeepaliveTime(dbProps.getKeepaliveTime());

        return dataSource;
    }
}

