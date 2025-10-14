package com.daangcool.stack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfiguration {

    @Bean
    public MessageMatcherDelegatingAuthorizationManager.Builder messages() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
            new MessageMatcherDelegatingAuthorizationManager.Builder();

        messages
            .nullDestMatcher().authenticated()
            .simpDestMatchers("/topic/tracker").hasRole("ADMIN")
            .simpDestMatchers("/topic/**").authenticated()
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll()
            .anyMessage().denyAll();

        return messages;
    }
}
