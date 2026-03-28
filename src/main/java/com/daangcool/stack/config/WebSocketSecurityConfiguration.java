package com.daangcool.stack.config;

import com.daangcool.stack.security.AuthoritiesConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfiguration {

    @Bean
    public AuthorizationManager<Message<?>> messages(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.OTHER).permitAll()
            .nullDestMatcher().permitAll()
            .simpSubscribeDestMatchers("/topic/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .simpMessageDestMatchers("/app/activity").authenticated()
            .simpDestMatchers("/topic/**").authenticated()
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).authenticated()
            .anyMessage().denyAll();

        return messages.build();
    }
}
