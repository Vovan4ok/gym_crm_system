package org.volodymyrzganiaiko.gym.crm.system.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDAO userDAO;

    public CustomUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> foundUserOpt = userDAO.findByUsername(username);
        if (foundUserOpt.isEmpty()) throw new UsernameNotFoundException("User with username " + username + "was not found");
        User foundUser = foundUserOpt.get();
        org.springframework.security.core.userdetails.User.UserBuilder userBuilder = org.springframework.security.core.userdetails.User.builder();
        userBuilder.username(foundUser.getUsername());
        userBuilder.password(foundUser.getPassword());
        userBuilder.authorities("ROLE_USER");
        userBuilder.disabled(!foundUser.getIsActive());
        return userBuilder.build();
    }
}
