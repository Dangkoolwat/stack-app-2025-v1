package com.daangcool.stack.repository;

import com.daangcool.stack.domain.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Settings} entity.
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
}
