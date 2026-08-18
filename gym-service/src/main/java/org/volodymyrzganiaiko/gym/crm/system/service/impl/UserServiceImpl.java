package org.volodymyrzganiaiko.gym.crm.system.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;
import org.volodymyrzganiaiko.gym.crm.system.service.UserService;

import static org.volodymyrzganiaiko.gym.crm.system.utils.ValueValidator.requireNotBlank;

@Service
public class UserServiceImpl implements UserService {
    private UserDAO userDAO;
    private PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void changePassword(String username, String newPassword) {
        log.info("Changing password for the user with username {}", username);
        requireNotBlank(newPassword, "password");
        User user = userDAO.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User with username " + username + " was not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
