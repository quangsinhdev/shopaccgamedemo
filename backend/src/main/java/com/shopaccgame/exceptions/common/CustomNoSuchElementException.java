package com.shopaccgame.exceptions.common;

import org.springframework.http.HttpStatus;

public class CustomNoSuchElementException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public CustomNoSuchElementException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public CustomNoSuchElementException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}