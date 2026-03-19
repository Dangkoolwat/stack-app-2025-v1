package com.daangcool.stack.domain;

import com.daangcool.stack.common.constant.Constants;
import com.daangcool.stack.domain.enumeration.SocialType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A user.
 */
@Entity
@Table(name = "stack_user")
@Getter
@Setter
// 2026-03-20: 인증 데이터 stale 방지를 위해 L2 캐시 제거 (보안 정책 적용)
public class User extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",
        sequenceName = "user_sequence_generator",
        allocationSize = 1
    )
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @Column(name = "social_type")
    @Enumerated(EnumType.STRING)
    private SocialType socialType ;

    //소셜 사용자 고유 ID (providerId)
    @Column(name = "provider_id", length = 200, unique = true)
    private String providerId;

    //마지막 로그인 시간
    @Column(name = "last_login_date")
    private Instant lastLoginDate;

    @Column(name = "login_failure_count")
    private int loginFailureCount = 0;

    @Column(name = "lock_reason", length = 255)
    private String lockReason;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    // 가입 탈퇴 여부
    @Column(nullable = false)
    private  boolean enabled = true;

    @Column(nullable = false)
    private  boolean accountNonExpired = true;

    @Column(nullable = false)
    private  boolean accountNonLocked = true;

    @Column(nullable = false)
    private  boolean credentialsNonExpired = true;


    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    //비밀번호 제설정
        @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    //extra
    private String description;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "stack_user_authority",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated='" + activated + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + activationKey + '\'' +
            "}";
    }
}
