package com.shopaccgame.exceptions.transaction.deposit;

import org.springframework.http.HttpStatus;

public class VNPayTransactionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public VNPayTransactionException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public VNPayTransactionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}