package com.shopaccgame.exceptions.user.authentication;

import org.springframework.http.HttpStatus;

public class UpdatePasswordException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public UpdatePasswordException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public UpdatePasswordException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}