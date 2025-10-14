package com.daangcool.stack.config;

import com.daangcool.stack.aop.logging.LoggingAspect;
import com.daangcool.stack.common.constant.StackAppConstants;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;


@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(StackAppConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }
}
