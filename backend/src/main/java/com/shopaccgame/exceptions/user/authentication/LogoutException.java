package com.shopaccgame.exceptions.user.authentication;

import org.springframework.http.HttpStatus;

public class LogoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public LogoutException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public LogoutException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}