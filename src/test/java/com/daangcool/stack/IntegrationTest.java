package com.daangcool.stack;

import com.daangcool.stack.config.AsyncSyncConfiguration;
import com.daangcool.stack.config.JacksonConfiguration;
import com.daangcool.stack.config.LiquibaseTestConfiguration;
import com.daangcool.stack.config.TestcontainersConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base composite annotation for integration tests.
 *
 * - Oracle: @ServiceConnection 으로 자동 프로퍼티 주입
 * - Redis: @DynamicPropertySource 로 jhipster.cache.redis.server 수동 주입
 *   (Redisson 기반이므로 @ServiceConnection 비호환)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        StackApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        LiquibaseTestConfiguration.class,
        TestcontainersConfiguration.class,
    },
    properties = {
        "jhipster.security.authentication.jwt.base64-secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "jhipster.security.authentication.jwt.token-validity-in-seconds=60000",
    }
)
@ActiveProfiles({"testdev", "test"})
public @interface IntegrationTest {
}
