---
name: spring-security-oauth2
description: >
  Spring Security 6 + OAuth2 Resource Server + JWT patterns for JHipster-based
  Spring Boot projects. Use when configuring security filters, JWT validation,
  authorization rules, method security, or writing security tests.
---

# Spring Security & OAuth2 Expert

Spring Security 6 + OAuth2 Resource Server 기반 인증/인가 패턴 가이드. JHipster 프로젝트에 최적화.

## 1. Architecture Overview

```
Client (Vue 3)
  ↓ Bearer Token (JWT)
Spring Security Filter Chain
  ↓ JwtAuthenticationConverter
SecurityContext → @PreAuthorize / @Secured
  ↓
Controller → Service → Repository
```

## 2. Security Filter Chain

### 2-1. 기본 구조
```java
/**
 * 보안 필터 체인 설정 (JHipster 기반)
 *
 * 역할:
 * - JWT 기반 stateless 인증
 * - CORS/CSRF 정책 적용
 * - 엔드포인트별 접근 제어
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // JWT 사용 시 CSRF 불필요
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/authenticate").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### 2-2. JWT Converter (권한 매핑)
```java
/**
 * JWT 클레임에서 Spring Security 권한으로 변환
 * JHipster의 'auth' 클레임을 GrantedAuthority로 매핑
 */
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("auth");
    grantedAuthoritiesConverter.setAuthorityPrefix("");

    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return converter;
}
```

## 3. Method Security Patterns

```java
// 관리자 전용 엔드포인트 (역할 기반 인가)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public void deleteUser(Long userId) { ... }

// 본인 데이터만 접근 가능 (소유권 기반 인가)
@PreAuthorize("#login == authentication.name or hasAuthority('ROLE_ADMIN')")
public UserDTO getUser(@PathVariable String login) { ... }

// SpEL로 복합 조건 표현
@PreAuthorize("@resourceAuthorizationService.canAccess(#id, authentication)")
public BoardDTO getBoard(@PathVariable Long id) { ... }
```

## 4. Security Test Patterns

```java
/**
 * 보안 테스트: 인증 없이 보호된 엔드포인트 접근 시 401 반환 확인
 */
@WebMvcTest(AccountResource.class)
class AccountResourceTest {

    @Test
    @WithUnauthenticatedMockUser
    void shouldReturn401ForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/account"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyUserAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }
}
```

## 5. Safety Rules

- **NEVER** 하드코딩된 시크릿 키 사용. `application.yml`의 `jhipster.security.authentication.jwt.base64-secret` 참조.
- **NEVER** `permitAll()`을 데이터 변경 엔드포인트에 적용.
- **MUST** CORS 설정은 `jhipster.cors` 프로퍼티를 통해 관리.
- **MUST** 보안 설정 변경 시 반드시 인증/비인증/역할별 3가지 테스트 케이스 작성.
- **Stateless**: 세션 사용 금지. JWT 토큰 기반 인증만 허용.

## 6. JHipster-Specific

- 인증 엔드포인트: `/api/authenticate` (JWT 발급)
- 계정 정보: `/api/account` (현재 사용자)
- 권한 체계: `ROLE_USER`, `ROLE_ADMIN` (JHipster default)
- 토큰 갱신: 프론트엔드 Axios interceptor에서 401 감지 후 처리

## 7. Korean Comment Rule

보안 관련 코드에는 반드시 한글 주석으로 *왜 이 제약이 필요한지* 설명한다:
```java
// 관리자만 사용자 삭제 가능 (데이터 무결성 보호)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public void deleteUser(Long id) { ... }
```
