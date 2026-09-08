package org.volodymyrzganiaiko.auth_service.security.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.auth_service.domain.AuthUser;
import org.volodymyrzganiaiko.auth_service.repository.AuthUserRepository;

@Service
public class CredentialVerificationService {
    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CredentialVerificationService(AuthUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void verify(String username, String rawPassword) {
        AuthUser user = repository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
