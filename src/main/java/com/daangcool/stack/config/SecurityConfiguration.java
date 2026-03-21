package com.daangcool.stack.config;

import com.daangcool.stack.security.AuthoritiesConstants;
import com.daangcool.stack.security.RateLimitingRegistry;
import com.daangcool.stack.security.handler.CustomAccessDeniedHandler;
import com.daangcool.stack.security.handler.CustomAuthenticationEntryPoint;
import com.daangcool.stack.web.filter.RateLimitingFilter;
import com.daangcool.stack.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import tech.jhipster.config.JHipsterProperties;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final ApplicationProperties applicationProperties;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final ObjectMapper objectMapper;
    private final RateLimitingRegistry rateLimitingRegistry;

    public SecurityConfiguration(
        JHipsterProperties jHipsterProperties,
        ApplicationProperties applicationProperties,
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
        CustomAccessDeniedHandler customAccessDeniedHandler,
        ObjectMapper objectMapper,
        RateLimitingRegistry rateLimitingRegistry
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.applicationProperties = applicationProperties;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.objectMapper = objectMapper;
        this.rateLimitingRegistry = rateLimitingRegistry;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(new RateLimitingFilter(objectMapper, applicationProperties, rateLimitingRegistry), BasicAuthenticationFilter.class)
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            .headers(headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    // Spring Boot 기본 정적 리소스 (ApplicationProperties 외부화)
                    .requestMatchers(applicationProperties.getSecurity().getPublicPaths().getStaticResources()).permitAll()

                    // JHipster가 추가로 서빙하는 정적 리소스
                    .requestMatchers(applicationProperties.getSecurity().getPublicPaths().getI18n()).permitAll()

                    // Swagger/OpenAPI 허용 경로
                    .requestMatchers(applicationProperties.getSecurity().getPublicPaths().getSwagger()).permitAll()

                    // 1. 공개 파일 경로: 인증 없이 접근 허용 (Static Resource Handler가 서빙)
                    .requestMatchers(applicationProperties.getFile().getPublicPath() + "/**").permitAll()

                    // 2. 비공개 파일 폴더 접근 완전 차단
                    .requestMatchers(applicationProperties.getFile().getPrivatePath() + "/**").denyAll()

                    // 나머지 API / 관리 / 인증 규칙
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                    .requestMatchers("/api/register").permitAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll()

                    // 파일 업로드/다운로드 API 보호
                    .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/uploads/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/uploads/**").hasAuthority(AuthoritiesConstants.ADMIN)

                    // OTP 인증 전용 (항상 공개)
                    .requestMatchers("/api/auth/email/**").permitAll()
                    // 로그인, 회원가입 등 인증 관련 공개 API
                    .requestMatchers("/api/auth/**").permitAll()
                    // 공용(public) 리소스
                    .requestMatchers("/api/public/**").permitAll()

                    // 관리자 전용 API
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)

                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers(applicationProperties.getSecurity().getPublicPaths().getWebsocket()).permitAll()
                    .requestMatchers(applicationProperties.getSecurity().getPublicPaths().getManagement()).permitAll()
                    .requestMatchers("/management/prometheus").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)

            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                //  CustomAuthenticationEntryPoint를 등록
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                //  JWT 디코더/인코더는 기존과 동일
                .jwt(withDefaults())
            );
        return http.build();
    }

}
