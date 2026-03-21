package com.daangcool.stack.security;

import com.daangcool.stack.common.constant.Constants;
import com.daangcool.stack.domain.Authority;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.UserRepository;
// Redis 인증 캐시 (ArchUnit 준수를 위해 security 패키지로 이동)
// import com.daangcool.stack.service.UserAuthCacheService;
// import com.daangcool.stack.service.dto.UserAuthCacheDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Authenticate a user from the database (with Redis 2nd-level cache).
 *
 * 캐시 전략:
 *  1단계 — Redis 조회 (auth:user:{login})
 *  2단계 — Cache MISS 시 DB 조회 (UserRepository)
 *  3단계 — DB 결과를 UserAuthCacheDto 로 변환 후 Redis 에 저장
 *
 * 무효화는 UserService 의 상태 변경 메서드에서 UserAuthCacheService.evict() 호출.
 *
 * 변경 이력:
 *  - 2026-03-20: C-1 인증 캐시 제거 (User JPA 엔티티 직렬화 문제)
 *  - 현재: UserAuthCacheDto(record) 기반 2차 캐시 재도입
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;
    private final UserAuthCacheService userAuthCacheService;

    public DomainUserDetailsService(
        UserRepository userRepository,
        UserAuthCacheService userAuthCacheService
    ) {
        this.userRepository = userRepository;
        this.userAuthCacheService = userAuthCacheService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String login) {
        LOG.debug("Authenticating {}", login);

        // ─────────────────────────────────────────────────────────────
        // 1단계: Redis 캐시 조회
        // ─────────────────────────────────────────────────────────────
        String cacheKey = login.toLowerCase(Locale.ENGLISH);
        var cached = userAuthCacheService.get(cacheKey);
        if (cached.isPresent()) {
            return toUserDetails(cached.get());
        }

        // ─────────────────────────────────────────────────────────────
        // 2단계: DB 조회 (Cache MISS)
        // ─────────────────────────────────────────────────────────────
        User user = resolveUser(login);

        // ─────────────────────────────────────────────────────────────
        // 3단계: Redis 에 캐시 저장 (트랜잭션 안이므로 LazyLoad 안전)
        // ─────────────────────────────────────────────────────────────
        userAuthCacheService.put(cacheKey, UserAuthCacheDto.from(user));

        return createSpringSecurityUser(login, user);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /** 이메일 / 로그인 분기 처리 후 User 반환 (Fallback 정책 적용) */
    private User resolveUser(String login) {
        // 1. 보안 규정에 따른 형식 검증
        if (!login.matches(Constants.LOGIN_REGEX)) {
            throw new UsernameNotFoundException("User " + login + " is not in a valid format");
        }

        // 2. 우선순위에 따른 조회 (이메일 -> 로그인)
        // 형식에 구애받지 않고 유연하게 사용자를 식별하기 위해 Fallback 정책을 사용합니다.
        return userRepository.findOneWithAuthoritiesByEmailIgnoreCase(login)
            .or(() -> userRepository.findOneWithAuthoritiesByLogin(login.toLowerCase(Locale.ENGLISH)))
            .orElseThrow(() ->
                new UsernameNotFoundException("User " + login + " was not found in the database"));
    }

    /** Redis 캐시 DTO → Spring Security UserDetails 변환 */
    private UserDetails toUserDetails(UserAuthCacheDto dto) {
        if (!dto.activated()) {
            throw new UserNotActivatedException("User " + dto.login() + " was not activated");
        }
        if (!dto.enabled()) {
            throw new UserNotActivatedException("User " + dto.login() + " is disabled");
        }
        if (!dto.accountNonLocked()) {
            throw new UserNotActivatedException("User " + dto.login() + " is locked");
        }
        List<SimpleGrantedAuthority> authorities = dto.authorities().stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
        return new UserWithId(dto.login(), "", authorities, dto.id());
    }

    /** DB 조회 결과 User → Spring Security UserDetails 변환 (기존 로직 유지) */
    private UserDetails createSpringSecurityUser(String lowercaseLogin, User user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        return UserWithId.fromUser(user);
    }

    // ------------------------------------------------------------------
    // Inner class: UserWithId (기존과 동일)
    // ------------------------------------------------------------------

    public static class UserWithId extends org.springframework.security.core.userdetails.User {

        private final Long id;

        public UserWithId(String login, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(login, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public static UserWithId fromUser(User user) {
            return new UserWithId(
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities().stream()
                    .map(Authority::getName)
                    .map(SimpleGrantedAuthority::new)
                    .toList(),
                user.getId()
            );
        }
    }
}
