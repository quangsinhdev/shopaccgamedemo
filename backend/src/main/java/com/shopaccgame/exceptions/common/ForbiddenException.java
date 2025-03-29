package com.shopaccgame.exceptions.common;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public ForbiddenException(String message) {
        super(message);
        this.status = HttpStatus.FORBIDDEN;
    }

    public ForbiddenException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}