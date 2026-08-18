package org.volodymyrzganiaiko.gym.crm.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.volodymyrzganiaiko.gym.crm.system.dao.UserDAO;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;
import org.volodymyrzganiaiko.gym.crm.system.utils.CredentialsGenerator;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CredentialsService {
    private UserDAO userDAO;
    private CredentialsGenerator credentialsGenerator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setUserDAO (UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Autowired
    public void setCredentialsGenerator (CredentialsGenerator credentialsGenerator) {
        this.credentialsGenerator = credentialsGenerator;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateUsername(User user) {
        Set<String> existingUsernames = userDAO.findAll().stream().map(User::getUsername).collect(Collectors.toSet());
        return credentialsGenerator.generateUsername(user, existingUsernames);
    }

    public String generatePassword() {
        return credentialsGenerator.generatePassword();
    }

    @Transactional(readOnly = true)
    public String assignCredentials(User user) {
        user.setIsActive(true);
        user.setUsername(generateUsername(user));
        String password = generatePassword();
        user.setPassword(passwordEncoder.encode(password));
        return password;
    }

}
