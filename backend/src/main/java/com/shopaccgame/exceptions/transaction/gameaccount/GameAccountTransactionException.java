package com.shopaccgame.exceptions.transaction.gameaccount;

import org.springframework.http.HttpStatus;

public class GameAccountTransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public GameAccountTransactionException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public GameAccountTransactionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}