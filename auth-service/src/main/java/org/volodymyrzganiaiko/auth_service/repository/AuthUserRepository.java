package org.volodymyrzganiaiko.auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.volodymyrzganiaiko.auth_service.domain.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUsername(String username);
}
