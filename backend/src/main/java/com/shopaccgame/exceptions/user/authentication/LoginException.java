package com.shopaccgame.exceptions.user.authentication;

import org.springframework.http.HttpStatus;

public class LoginException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public LoginException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public LoginException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}