package com.daangcool.stack.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseConfigurationTest {

    @Test
    @DisplayName("HikariCP 설정: 자동 CPU 계산(maxPoolSize=0) 및 고정 풀(minimumIdle=-1) 설정 확인")
    void shouldCalculatePoolSizeAutomaticallyAndSetFixedPool() {
        // given
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:oracle:thin:@localhost:1521:xe");
        env.setProperty("spring.datasource.username", "sa");
        env.setProperty("spring.datasource.password", "");
        env.setProperty("spring.datasource.driver-class-name", "oracle.jdbc.OracleDriver");

        ApplicationProperties props = new ApplicationProperties();
        ApplicationProperties.Database dbProps = props.getDatabase();
        
        // 0이면 자동 계산 활성화
        dbProps.setMaxPoolSize(0);
        // -1이면 maxPoolSize와 동일하게 맞춤 (고정 풀)
        dbProps.setMinimumIdle(-1);
        dbProps.setConnectionTimeout(12345L);
        dbProps.setIdleTimeout(67890L);
        dbProps.setMaxLifetime(99999L);
        dbProps.setKeepaliveTime(11111L);

        DatabaseConfiguration config = new DatabaseConfiguration();

        // when
        HikariDataSource dataSource = config.dataSource(env, props);

        // then
        int cores = Runtime.getRuntime().availableProcessors();
        int expectedPoolSize = (cores * 2) + 1;

        assertThat(dataSource.getJdbcUrl()).isEqualTo("jdbc:oracle:thin:@localhost:1521:xe");
        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(expectedPoolSize);
        // minimumIdle가 -1이므로 자동 계산된 expectedPoolSize와 정확히 같아야 함
        assertThat(dataSource.getMinimumIdle()).isEqualTo(expectedPoolSize);
        
        assertThat(dataSource.getConnectionTimeout()).isEqualTo(12345L);
        assertThat(dataSource.getIdleTimeout()).isEqualTo(67890L);
        assertThat(dataSource.getMaxLifetime()).isEqualTo(99999L);
        // keepaliveTime은 getter가 없는 경우가 있으므로 생략하거나, 가능하다면 단언
    }

    @Test
    @DisplayName("HikariCP 설정: 명시적 풀 사이즈와 최소 아이들 설정이 있을 때 확인")
    void shouldUseExplicitValuesWhenProvided() {
        // given
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:oracle:thin:@localhost:1521:xe");

        ApplicationProperties props = new ApplicationProperties();
        ApplicationProperties.Database dbProps = props.getDatabase();
        
        // 사용자가 명시적으로 값을 지정했을 경우
        dbProps.setMaxPoolSize(15);
        dbProps.setMinimumIdle(5);

        DatabaseConfiguration config = new DatabaseConfiguration();

        // when
        HikariDataSource dataSource = config.dataSource(env, props);

        // then
        assertThat(dataSource.getMaximumPoolSize()).isEqualTo(15);
        assertThat(dataSource.getMinimumIdle()).isEqualTo(5);
    }
}
