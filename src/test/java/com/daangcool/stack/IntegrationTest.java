package com.daangcool.stack;

import com.daangcool.stack.config.EmbeddedRedis;
import com.daangcool.stack.config.EmbeddedSQL;
import com.daangcool.stack.config.LiquibaseTestConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {StackApp.class, LiquibaseTestConfiguration.class})
@EmbeddedRedis
@EmbeddedSQL
public @interface IntegrationTest {
}
