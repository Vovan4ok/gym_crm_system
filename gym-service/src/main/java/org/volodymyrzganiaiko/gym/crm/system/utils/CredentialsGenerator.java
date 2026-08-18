package org.volodymyrzganiaiko.gym.crm.system.utils;

import org.springframework.stereotype.Component;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;
import java.util.*;

@Component
public class CredentialsGenerator {
    private final SecureRandom secureRandom = new SecureRandom();
    private static final short PASSWORD_LENGTH = 10;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Logger log = LoggerFactory.getLogger(CredentialsGenerator.class);

    public String generateUsername(User user, Set<String> existingUsernames) {
        log.debug("Generating username for user {} {}", user.getFirstName(), user.getLastName());
        String requiredUsername = user.getFirstName() + "." + user.getLastName();
        if (!existingUsernames.contains(requiredUsername)) {
            return requiredUsername;
        } else {
            int count = 1;
            while (true) {
                String base = requiredUsername + "." + count;
                if (!existingUsernames.contains(base)) {
                    return base;
                }
                count++;
            }
        }
    }

    public String generatePassword() {
        log.debug("Generating password");
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }
}
