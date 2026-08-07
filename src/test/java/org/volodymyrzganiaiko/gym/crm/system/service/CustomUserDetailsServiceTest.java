package org.volodymyrzganiaiko.gym.crm.system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    public void foundUser_active() {
        User user = new Trainee();
        user.setUsername("John.Doe");
        user.setPassword("random");
        user.setIsActive(true);
        when(userDAO.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("John.Doe");

        assertEquals("John.Doe", result.getUsername());
        assertEquals("random", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(result.isEnabled());
    }

    @Test
    public void foundUser_inactive() {
        User user = new Trainee();
        user.setUsername("John.Doe");
        user.setPassword("random");
        user.setIsActive(false);
        when(userDAO.findByUsername("John.Doe")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("John.Doe");

        assertFalse(result.isEnabled());
    }

    @Test
    public void userNotFound() {
        when(userDAO.findByUsername("Ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("Ghost"));
    }
}
