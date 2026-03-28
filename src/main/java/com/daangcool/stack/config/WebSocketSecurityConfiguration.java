package com.daangcool.stack.config;

import com.daangcool.stack.security.AuthoritiesConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfiguration {

    /**
     * JWT 기반 Stateless 인증에서는 세션/쿠키 기반 CSRF 보호가 불필요합니다.
     * @EnableWebSocketSecurity 가 기본 등록하는 csrfChannelInterceptor 를
     * no-op 으로 대체하여 STOMP CONNECT 프레임이 CSRF 검증 없이 통과하도록 합니다.
     */
    @Bean("csrfChannelInterceptor")
    public ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }

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
