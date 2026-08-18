package org.volodymyrzganiaiko.gym.crm.system.security.exception;

public class UserBlockedException extends RuntimeException {
    public UserBlockedException(String message) {
        super(message);
    }
}
