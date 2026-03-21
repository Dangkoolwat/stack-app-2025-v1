package com.daangcool.stack.security;

import com.daangcool.stack.domain.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [UserAuthCacheDto] Redis 2차 캐시 전용 인증 DTO
 *
 * 역할:
 * - 인증에 필요한 최소한의 사용자 정보를 불변 객체로 캐시함
 * - JPA 엔티티(User)를 직접 캐시할 때 발생하는 Hibernate Proxy 직렬화 문제 해결
 *
 * 에이전트 작업 가이드:
 * - 필드 추가 시 DomainUserDetailsService.toUserDetails() 에도 반영해야 함
 * - Jackson 3의 NON_FINAL 타이핑 호환성을 위해 record 가 아닌 일반 class 로 유지
 *
 * 주의사항:
 * - 직렬화 호환성을 위해 필드 순서나 타입 변경 시 신중해야 함
 *
 * 변경 이력:
 * - 2026-03-21: [Refactor] Jackson NON_FINAL 호환성을 위해 record -> class 전환
 */
@Getter
@Setter
@NoArgsConstructor
public class UserAuthCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String login;
    private String password;
    private String email;
    private boolean activated;
    private boolean enabled;
    private boolean accountNonLocked;
    private Set<String> authorities;

    public UserAuthCacheDto(
        Long id,
        String login,
        String password,
        String email,
        boolean activated,
        boolean enabled,
        boolean accountNonLocked,
        Set<String> authorities
    ) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.activated = activated;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    // record 스타일의 getter 메서드 유지 (기존 코드와 호환성)
    public Long id() { return id; }
    public String login() { return login; }
    public String password() { return password; }
    public String email() { return email; }
    public boolean activated() { return activated; }
    public boolean enabled() { return enabled; }
    public boolean accountNonLocked() { return accountNonLocked; }
    public Set<String> authorities() { return authorities; }

    /**
     * User JPA 엔티티 → UserAuthCacheDto 변환
     *
     * @param user 영속 상태의 User 엔티티
     * @return 캐시 저장용 DTO
     */
    public static UserAuthCacheDto from(User user) {
        Set<String> authNames = user.getAuthorities().stream()
            .map(auth -> auth.getName())
            .collect(Collectors.toUnmodifiableSet());

        return new UserAuthCacheDto(
            user.getId(),
            user.getLogin(),
            user.getPassword(),
            user.getEmail(),
            user.isActivated(),
            user.isEnabled(),
            user.isAccountNonLocked(),
            authNames
        );
    }
}
