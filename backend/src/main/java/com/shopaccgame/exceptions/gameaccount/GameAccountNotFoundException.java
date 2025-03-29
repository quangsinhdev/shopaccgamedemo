package com.shopaccgame.exceptions.gameaccount;

import org.springframework.http.HttpStatus;

public class GameAccountNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public GameAccountNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public GameAccountNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}