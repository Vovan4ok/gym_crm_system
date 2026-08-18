package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;
import org.volodymyrzganiaiko.gym.crm.system.utils.CredentialsGenerator;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CredentialsServiceTest {
    @Mock
    private UserDAO userDAO;

    @Mock
    private CredentialsGenerator credentialsGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CredentialsService credentialsService;

    @Test
    void generateUsername_collectsUsernames_andDelegates() {
        Trainee input = new Trainee(null, null, null, null, "A.One", null, true, null);
        when(userDAO.findAll()).thenReturn(List.of(input, new Trainee(null, null, null, null, "B.Two", null, true, null)));
        when(credentialsGenerator.generateUsername(any(), any())).thenReturn("A.One.1");

        String result = credentialsService.generateUsername(input);

        assertEquals("A.One.1", result);
        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(credentialsGenerator).generateUsername(any(User.class), captor.capture());
        assertTrue(captor.getValue().containsAll(Set.of("A.One", "B.Two")));
    }

    @Test
    void generatePassword_delegatesToGenerator() {
        when(credentialsGenerator.generatePassword()).thenReturn("secret1234");
        assertEquals("secret1234", credentialsService.generatePassword());
        verify(credentialsGenerator).generatePassword();
    }

    @Test
    void assignCredentials_success() {
        User user = new Trainee();
        user.setIsActive(false);

        when(userDAO.findAll()).thenReturn(List.of());
        when(credentialsGenerator.generateUsername(any(), any())).thenReturn("John.Doe");
        when(credentialsGenerator.generatePassword()).thenReturn("secret1234");
        when(passwordEncoder.encode("secret1234")).thenReturn("secret1234Encoded");

        String result = credentialsService.assignCredentials(user);
        assertEquals("secret1234", result);
        assertEquals("secret1234Encoded", user.getPassword());
        assertNotEquals(result, user.getPassword());
        assertEquals("John.Doe", user.getUsername());
        assertTrue(user.getIsActive());
    }
}
