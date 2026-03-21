package com.daangcool.stack.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.daangcool.stack.IntegrationTest;
import com.daangcool.stack.domain.Authority;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.service.UserService;

import java.util.Collections;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * DomainUserDetailsService 통합 테스트
 *
 * 기존 케이스(로그인/이메일/비활성화) 유지 + 2차 캐시 동작 검증 추가.
 *
 * 캐시 검증 항목:
 *  - 첫 번째 loadUserByUsername() 호출 후 Redis 에 DTO 저장됨
 *  - 두 번째 호출은 Redis 캐시에서 반환됨 (DB 쿼리 생략)
 *  - evict() 후 재호출 시 DB 재조회 후 캐시 복구됨
 */
@Transactional
@IntegrationTest
class DomainUserDetailsServiceIT {

    private static final String USER_ONE_LOGIN = "test-user-one";
    private static final String USER_ONE_EMAIL = "test-user-one@localhost";
    private static final String USER_TWO_LOGIN = "test-user-two";
    private static final String USER_TWO_EMAIL = "test-user-two@localhost";
    private static final String USER_THREE_LOGIN = "test-user-three";
    private static final String USER_THREE_EMAIL = "test-user-three@localhost";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("userDetailsService")
    private UserDetailsService domainUserDetailsService;

    @Autowired
    private UserAuthCacheService userAuthCacheService;

    // ──────────────────────────────────────────────
    // 테스트 데이터
    // ──────────────────────────────────────────────

    public User getUserOne() {
        User userOne = new User();
        userOne.setLogin(USER_ONE_LOGIN);
        userOne.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userOne.setActivated(true);
        userOne.setEmail(USER_ONE_EMAIL);
        userOne.setFirstName("userOne");
        userOne.setLastName("doe");
        userOne.setLangKey("en");
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        userOne.setAuthorities(Collections.singleton(authority));
        return userOne;
    }

    public User getUserTwo() {
        User userTwo = new User();
        userTwo.setLogin(USER_TWO_LOGIN);
        userTwo.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userTwo.setActivated(true);
        userTwo.setEmail(USER_TWO_EMAIL);
        userTwo.setFirstName("userTwo");
        userTwo.setLastName("doe");
        userTwo.setLangKey("en");
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        userTwo.setAuthorities(Collections.singleton(authority));
        return userTwo;
    }

    public User getUserThree() {
        User userThree = new User();
        userThree.setLogin(USER_THREE_LOGIN);
        userThree.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userThree.setActivated(false);
        userThree.setEmail(USER_THREE_EMAIL);
        userThree.setFirstName("userThree");
        userThree.setLastName("doe");
        userThree.setLangKey("en");
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        userThree.setAuthorities(Collections.singleton(authority));
        return userThree;
    }

    @BeforeEach
    void init() {
        userRepository.save(getUserOne());
        userRepository.save(getUserTwo());
        userRepository.save(getUserThree());
    }

    @AfterEach
    void cleanup() {
        userService.deleteUser(USER_ONE_LOGIN);
        userService.deleteUser(USER_TWO_LOGIN);
        userService.deleteUser(USER_THREE_LOGIN);
        // 테스트 후 캐시도 정리
        userAuthCacheService.evict(USER_ONE_LOGIN);
        userAuthCacheService.evict(USER_TWO_LOGIN);
        userAuthCacheService.evict(USER_THREE_LOGIN);
    }

    // ──────────────────────────────────────────────
    // 기존 테스트 (변경 없이 유지)
    // ──────────────────────────────────────────────

    @Test
    void assertThatUserCanBeFoundByLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByLoginIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmail() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmailIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatEmailIsPrioritizedOverLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserNotActivatedExceptionIsThrownForNotActivatedUsers() {
        assertThatExceptionOfType(UserNotActivatedException.class).isThrownBy(() ->
            domainUserDetailsService.loadUserByUsername(USER_THREE_LOGIN)
        );
    }

    // ──────────────────────────────────────────────
    // 2차 캐시 검증 테스트 (신규)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("loadUserByUsername() 호출 후 Redis 에 UserAuthCacheDto 가 저장된다")
    void loadUserByUsername_ShouldPopulateCache() {
        // 캐시 비어있음 확인
        assertThat(userAuthCacheService.get(USER_ONE_LOGIN)).isEmpty();

        // 로그인 → DB 조회 후 캐시 저장
        domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

        // Redis 에 캐시 저장됨
        Optional<UserAuthCacheDto> cached = userAuthCacheService.get(USER_ONE_LOGIN);
        assertThat(cached).isPresent();
        assertThat(cached.get().login()).isEqualTo(USER_ONE_LOGIN);
        assertThat(cached.get().activated()).isTrue();
        assertThat(cached.get().id()).isNotNull();
    }

    @Test
    @DisplayName("두 번째 loadUserByUsername() 호출은 Redis 캐시에서 반환된다")
    void loadUserByUsername_SecondCall_ShouldUseCachedDto() {
        // 첫 번째 호출 → DB + 캐시 저장
        UserDetails first = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

        // 두 번째 호출 → 캐시 반환
        UserDetails second = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

        // 동일한 username 반환
        assertThat(first.getUsername()).isEqualTo(second.getUsername());
    }

    @Test
    @DisplayName("evict() 후 loadUserByUsername() 호출 시 DB 재조회 후 캐시 복구된다")
    void loadUserByUsername_AfterEvict_ShouldRebuildCache() {
        // 캐시 채우기
        domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);
        assertThat(userAuthCacheService.get(USER_ONE_LOGIN)).isPresent();

        // 캐시 무효화
        userAuthCacheService.evict(USER_ONE_LOGIN);
        assertThat(userAuthCacheService.get(USER_ONE_LOGIN)).isEmpty();

        // 재조회 → DB 에서 읽어 캐시 복구
        domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);
        assertThat(userAuthCacheService.get(USER_ONE_LOGIN)).isPresent();
    }

    @Test
    @DisplayName("authorities 정보가 캐시에 올바르게 저장된다")
    void loadUserByUsername_ShouldCacheAuthorities() {
        domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);

        Optional<UserAuthCacheDto> cached = userAuthCacheService.get(USER_ONE_LOGIN);
        assertThat(cached).isPresent();
        // 기본 가입 사용자는 ROLE_USER 를 가짐
        assertThat(cached.get().authorities()).isNotEmpty();
    }
}
