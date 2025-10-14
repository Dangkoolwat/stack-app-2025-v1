package com.daangcool.stack.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

/**
 * Redis Testcontainers 설정 팩토리
 * ---------------------------------------------------
 * - @EmbeddedRedis 어노테이션이 감지되면 RedisTestContainer를 등록
 * - Redis 컨테이너 호스트/포트를 Spring Context에 주입
 * - getContainerIpAddress() → getHost() 로 교체 (deprecation 해결)
 */
public class RedisTestContainersSpringContextCustomizerFactory implements ContextCustomizerFactory {

    private static final Logger log = LoggerFactory.getLogger(RedisTestContainersSpringContextCustomizerFactory.class);

    private static RedisTestContainer redisBean;

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return new ContextCustomizer() {
            @Override
            public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
                ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                TestPropertyValues testValues = TestPropertyValues.empty();
                EmbeddedRedis redisAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedRedis.class);

                if (redisAnnotation != null) {
                    log.debug("Detected the @EmbeddedRedis annotation on class {}", testClass.getName());
                    log.info("Warming up the Redis TestContainer...");

                    if (redisBean == null) {
                        redisBean = beanFactory.createBean(RedisTestContainer.class);
                        beanFactory.registerSingleton(RedisTestContainer.class.getName(), redisBean);
                    }

                    // ✅ getContainerIpAddress() → getHost() 로 변경 (deprecation 해결)
                    String redisUrl = String.format(
                        "redis://%s:%d",
                        redisBean.getRedisContainer().getHost(),
                        redisBean.getRedisContainer().getMappedPort(6379)
                    );

                    testValues = testValues.and("jhipster.cache.redis.server=" + redisUrl);
                    log.info("Redis TestContainer started at {}", redisUrl);
                }

                testValues.applyTo(context);
            }

            @Override
            public int hashCode() {
                return RedisTestContainer.class.getName().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this.hashCode() == obj.hashCode();
            }
        };
    }
}
