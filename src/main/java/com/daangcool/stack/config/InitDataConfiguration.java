package com.daangcool.stack.config;

import com.daangcool.stack.domain.Authority;
import com.daangcool.stack.domain.User;
import com.daangcool.stack.domain.enumeration.SocialType;
import com.daangcool.stack.repository.AuthorityRepository;
import com.daangcool.stack.repository.UserRepository;
import com.daangcool.stack.security.AuthoritiesConstants;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Profile("data")
public class InitDataConfiguration {

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public InitDataConfiguration(AuthorityRepository authorityRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authorityRepository = authorityRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostConstruct
    public void initiateData() {

        Set<Authority> authorityList = Stream.of(new Authority(AuthoritiesConstants.ADMIN),
                new Authority(AuthoritiesConstants.USER))
            .collect(Collectors.toSet());
        authorityRepository.saveAll(authorityList);



        List<User> userList = new ArrayList<>();

        User user1 = new User();
        user1.setLogin("admin@localhost");
        user1.setPassword(passwordEncoder.encode("admin/!"));
        user1.setFirstName("관지라이름");
        user1.setLastName("관리자성");
        user1.setSocialType(SocialType.LOCAL);
        user1.setEmail("admin@localhost");
        user1.setActivated(true);
        user1.setEnabled(true);
        user1.setLangKey(Constants.DEFAULT_LANGUAGE);
        user1.setAccountNonExpired(true);
        user1.setAccountNonLocked(true);
        user1.setCredentialsNonExpired(true);
        user1.setCreatedBy(Constants.SYSTEM_ACCOUNT);
        user1.setCreatedDate(Instant.now());
        user1.setLastModifiedBy(Constants.SYSTEM_ACCOUNT);
        user1.setLastModifiedDate(Instant.now());

        Set<Authority> authorities = Stream.of(new Authority(AuthoritiesConstants.ADMIN),
                  new Authority(AuthoritiesConstants.USER))
            .collect(Collectors.toSet());
        user1.setAuthorities(authorities);

        userList.add(user1);

        User user2 = new User();
        user2.setLogin("user@localhost");
        user2.setPassword(passwordEncoder.encode("user/!"));
        user2.setFirstName("일반사용자성");
        user2.setLastName("일반사용자이름");
        user2.setSocialType(SocialType.LOCAL);
        user2.setEmail("user@localhost");
        user2.setActivated(true);
        user2.setEnabled(true);
        user2.setLangKey(Constants.DEFAULT_LANGUAGE);
        user2.setAccountNonExpired(true);
        user2.setAccountNonLocked(true);
        user2.setCredentialsNonExpired(true);
        user2.setCreatedBy(Constants.SYSTEM_ACCOUNT);
        user2.setCreatedDate(Instant.now());
        user2.setLastModifiedBy(Constants.SYSTEM_ACCOUNT);
        user2.setLastModifiedDate(Instant.now());

        Set<Authority> authorities2 = Collections.singleton(new Authority(AuthoritiesConstants.USER));
        user2.setAuthorities(authorities2);
        userList.add(user2);

        userRepository.saveAll(userList);


    }

}
