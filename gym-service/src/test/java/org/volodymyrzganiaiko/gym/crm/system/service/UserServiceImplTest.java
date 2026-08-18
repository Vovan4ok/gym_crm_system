package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;
import org.volodymyrzganiaiko.gym.crm.system.service.impl.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserDAO userDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void changePassword_success() {
        User user = new Trainee();
        user.setPassword("oldEncoded");

        when(userDAO.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword("John.Doe", "newPassword");

        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordEncoder).encode("newPassword");
        assertNotEquals("newPassword", user.getPassword());
    }

    @Test
    public void changePassword_blankPassword() {
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("John.Doe", ""));
        verifyNoInteractions(userDAO, passwordEncoder);
    }

    @Test
    public void changePassword_userNotFound() {
        when(userDAO.findByUsername("John.Doe")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->  userService.changePassword("John.Doe", "newPassword"));
        verifyNoInteractions(passwordEncoder);
    }
}
