package org.volodymyrzganiaiko.gym.crm.system.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    public void changeStatus_activate() {
        User user = new Trainee();
        user.setIsActive(false);

        user.changeStatus(true);

        assertTrue(user.getIsActive());
    }

    @Test
    public void changeStatus_deactivate() {
        User user = new Trainee();
        user.setIsActive(true);

        user.changeStatus(false);

        assertFalse(user.getIsActive());
    }

    @Test
    public void changeStatus_activateThrowsException() {
        User user = new Trainee();
        user.setIsActive(true);

        assertThrows(IllegalStateException.class, () -> user.changeStatus(true));
        assertTrue(user.getIsActive());
    }

    @Test
    public void changeStatus_deactivateThrowsException() {
        User user = new Trainee();
        user.setIsActive(false);
        user.setUsername("John.Doe");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> user.changeStatus(false));
        assertFalse(user.getIsActive());
        assertTrue(ex.getMessage().contains("inactive"));
        assertTrue(ex.getMessage().contains("John.Doe"));
    }
}
