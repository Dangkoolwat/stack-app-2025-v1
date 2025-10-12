package com.daangcool.stack.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.Settings} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private Long tokenValiditySeconds;

    @NotNull
    private Long tokenValiditySecondsForRememberMe;

    @NotNull
    private int loginMaxFailureAttempts;

    private String description;
}
