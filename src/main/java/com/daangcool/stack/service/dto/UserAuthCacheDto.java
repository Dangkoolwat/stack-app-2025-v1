package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 2차 캐시 전용 인증 DTO
 * ------------------------------------------------------------------
 * 설계 원칙:
 *  1. JPA 엔티티(User)를 직접 캐시하지 않는다.
 *     → Hibernate Proxy / LazyLoading / HibernateProxy$$xxxx @class 문제 원천 차단
 *  2. Jackson 3 직렬화에 안전한 단순 타입만 사용 (primitives, String, Set<String>)
 *     → DefaultTyping 활성화 상태에서도 @class 가 안정적으로 저장/복원
 *  3. 민감 정보(password, activationKey, resetKey) 완전 제외
 *  4. Java record 사용 → 불변(Immutable), 기본 생성자 불필요, equals/hashCode 자동
 *
 * 저장 키 패턴: "auth:user:{login}"
 * TTL: 5분 (application.auth-cache.ttl-minutes 로 외부화)
 * ------------------------------------------------------------------
 */
public record UserAuthCacheDto(
    Long id,
    String login,
    String email,
    boolean activated,
    boolean enabled,
    boolean accountNonLocked,
    Set<String> authorities
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User JPA 엔티티 → UserAuthCacheDto 변환
     *
     * 반드시 영속성 컨텍스트(트랜잭션) 안에서 호출해야 합니다.
     * authorities.getName() 호출로 LazyLoad를 이 시점에 해소합니다.
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
            user.getEmail(),
            user.isActivated(),
            user.isEnabled(),
            user.isAccountNonLocked(),
            authNames
        );
    }
}
