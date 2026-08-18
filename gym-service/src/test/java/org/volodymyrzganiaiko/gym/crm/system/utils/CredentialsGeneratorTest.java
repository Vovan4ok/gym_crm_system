package org.volodymyrzganiaiko.gym.crm.system.utils;

import org.junit.jupiter.api.Test;
import org.volodymyrzganiaiko.gym.crm.system.domain.Trainee;
import org.volodymyrzganiaiko.gym.crm.system.domain.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CredentialsGeneratorTest {
    private final CredentialsGenerator generator = new CredentialsGenerator();

    @Test
    void noCollision_returnsFirstDotLast() {
        assertEquals("John.Doe", generator.generateUsername(user("John","Doe"), Set.of()));
    }
    @Test
    void collision_appendsSuffix() {
        assertEquals("John.Doe.1", generator.generateUsername(user("John","Doe"), Set.of("John.Doe")));
    }
    @Test
    void doubleCollision_incrementsSuffix() {
        assertEquals("John.Doe.2", generator.generateUsername(user("John","Doe"), Set.of("John.Doe","John.Doe.1")));
    }
    @Test
    void password_is10Chars_fromAlphabet() {
        String p = generator.generatePassword();
        assertNotNull(p);
        assertEquals(10, p.length());
        assertTrue(p.matches("[A-Za-z0-9]+"));
    }

    private User user(String firstName, String lastName) {
        User testTrainee = new Trainee();
        testTrainee.setFirstName(firstName);
        testTrainee.setLastName(lastName);
        return testTrainee;
    }
}
