package org.volodymyrzganiaiko.auth_service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.volodymyrzganiaiko.auth_service.exception.UserBlockedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserBlockedException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public void handleBlocked(UserBlockedException e) {
        log.warn("Too many failed login attempts");
    }
}
