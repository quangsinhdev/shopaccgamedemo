package com.shopaccgame.exceptions.user;

import org.springframework.http.HttpStatus;

public class UpdateUserStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public UpdateUserStatusException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public UpdateUserStatusException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}