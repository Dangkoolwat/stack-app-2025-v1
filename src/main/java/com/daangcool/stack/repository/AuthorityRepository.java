package com.daangcool.stack.repository;

import com.daangcool.stack.domain.Authority;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
