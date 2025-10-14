package com.daangcool.stack.config;

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

import java.util.List;

/**
 * SQL Testcontainers 설정 팩토리
 * ------------------------------------------------------------
 * - @EmbeddedSQL 감지 시 SQL TestContainer 생성 및 등록
 * - datasource.url / username / password 를 Spring Context에 주입
 * - Class.forName()의 비검사 캐스팅 제거 (경고 해결)
 */
public class SqlTestContainersSpringContextCustomizerFactory implements ContextCustomizerFactory {

    private static final Logger log = LoggerFactory.getLogger(SqlTestContainersSpringContextCustomizerFactory.class);

    private static SqlTestContainer prodTestContainer;

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return new ContextCustomizer() {
            @Override
            public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
                ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                TestPropertyValues testValues = TestPropertyValues.empty();
                EmbeddedSQL sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL.class);

                if (sqlAnnotation != null) {
                    log.debug("Detected @EmbeddedSQL annotation on class {}", testClass.getName());
                    log.info("Starting SQL TestContainer...");

                    if (prodTestContainer == null) {
                        try {
                            // ✅ 안전한 제네릭 캐스팅 (unchecked warning 제거)
                            Class<?> rawClass = Class.forName(this.getClass().getPackageName() + ".TestContainer");

                            if (!SqlTestContainer.class.isAssignableFrom(rawClass)) {
                                throw new IllegalStateException("TestContainer must implement SqlTestContainer");
                            }

                            @SuppressWarnings("unchecked")
                            Class<? extends SqlTestContainer> containerClass =
                                (Class<? extends SqlTestContainer>) rawClass;

                            prodTestContainer = beanFactory.createBean(containerClass);
                            beanFactory.registerSingleton(containerClass.getName(), prodTestContainer);

                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("TestContainer class not found", e);
                        }
                    }

                    var testContainer = prodTestContainer.getTestContainer();
                    testValues = testValues.and(
                        "spring.datasource.url=" + testContainer.getJdbcUrl(),
                        "spring.datasource.username=" + testContainer.getUsername(),
                        "spring.datasource.password=" + testContainer.getPassword()
                    );
                }

                testValues.applyTo(context);
            }

            @Override
            public int hashCode() {
                return SqlTestContainer.class.getName().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return obj != null && this.hashCode() == obj.hashCode();
            }
        };
    }
}
