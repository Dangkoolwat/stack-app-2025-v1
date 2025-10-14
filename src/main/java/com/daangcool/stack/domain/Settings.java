package com.daangcool.stack.domain;

import com.daangcool.stack.common.constant.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "stack_settings")
@Getter
@Setter
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Settings extends AbstractAuditingEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    // 토큰 유효기간
    @Column(name = "token_validity_seconds")
    private Long tokenValiditySeconds;

    // remember-me 토큰 유효기간
    @Column(name = "token_validity_seconds_for_remember_me")
    private Long tokenValiditySecondsForRememberMe;

    @Column(name = "login_nmax_failure_attempts")
    private int loginMaxFailureAttempts = Constants.MAX_ATTEMPT;


    private String description;

}
