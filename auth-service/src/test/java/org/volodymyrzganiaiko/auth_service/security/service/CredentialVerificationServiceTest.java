package org.volodymyrzganiaiko.auth_service.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.auth_service.domain.AuthUser;
import org.volodymyrzganiaiko.auth_service.repository.AuthUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CredentialVerificationServiceTest {
    @Mock
    private AuthUserRepository authUserRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private CredentialVerificationService service;

    @BeforeEach
    public void setUp() {
        service = new CredentialVerificationService(authUserRepository, passwordEncoder);
    }

    @Test
    public void valid() {
        String hash = passwordEncoder.encode("secret");
        AuthUser user = mock(AuthUser.class);
        when(user.getIsActive()).thenReturn(true);
        when(user.getPassword()).thenReturn(hash);
        when(authUserRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> service.verify("John.Doe", "secret"));
    }

    @Test
    public void wrongPassword() {
        String hash = passwordEncoder.encode("secret");
        AuthUser user = mock(AuthUser.class);
        when(user.getIsActive()).thenReturn(true);
        when(user.getPassword()).thenReturn(hash);
        when(authUserRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        assertThrows(BadCredentialsException.class, () -> service.verify("John.Doe", "wrong"));
    }

    @Test
    public void unknownUser() {
        when(authUserRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> service.verify("nobody", "x"));
    }

    @Test
    public void inactiveUser() {
        AuthUser user = mock(AuthUser.class);
        when(user.getIsActive()).thenReturn(false);
        when(authUserRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        assertThrows(BadCredentialsException.class, () -> service.verify("John.Doe", "secret"));
    }
}
