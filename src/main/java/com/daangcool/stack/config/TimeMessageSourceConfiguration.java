package com.daangcool.stack.config;

import com.daangcool.stack.web.rest.errors.ProblemUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.TimeZone;

/**
 * TimeMessageSourceConfiguration
 * -----------------------------------------------------------
 * - 다국어(i18n) 메시지 설정
 * - 전역 시간대(Asia/Seoul) 통일
 * - RFC7807 ProblemUtils에 MessageSource 주입
 * -----------------------------------------------------------
 */
@Configuration
public class TimeMessageSourceConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(TimeMessageSourceConfiguration.class);
    private final MessageSource messageSource;

    public TimeMessageSourceConfiguration(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** 1. 전역 TimeZone 설정 (JVM 레벨에서 통일) */
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        ProblemUtils.setMessageSource(messageSource);  // 자동 MessageSource 연결
        log.info(":: Default TimeZone = {}, MessageSource linked", TimeZone.getDefault().getID());
    }

    /** 2. ISO 8601 날짜 포맷 등록 (Spring MVC 요청/응답 일관성 유지) */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
        log.info(":: ISO DateTime format registered.");
    }

    /** 3. MessageSourceAccessor (서비스나 컨트롤러에서도 간단히 사용 가능) */
    @Bean
    public MessageSourceAccessor messageSourceAccessor() {
        return new MessageSourceAccessor(messageSource);
    }
 }
