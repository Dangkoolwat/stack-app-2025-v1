package com.daangcool.stack.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    // Spring Boot 4 + Jackson 3 환경에서는
    // Jdk8Module/JavaTimeModule 수동 등록을 제거하고
    // 기본 Jackson auto-configuration을 사용합니다.

    /**
     * Support for Java date and time API.
     * @return the corresponding Jackson module.
     */
//    @Bean
//    public JavaTimeModule javaTimeModule() {
//        return new JavaTimeModule();
//    }
//
//    @Bean
//    public Jdk8Module jdk8TimeModule() {
//        return new Jdk8Module();
//    }
}
