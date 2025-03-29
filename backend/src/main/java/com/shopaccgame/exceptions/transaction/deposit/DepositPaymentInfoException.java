package com.shopaccgame.exceptions.transaction.deposit;

import org.springframework.http.HttpStatus;

public class DepositPaymentInfoException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public DepositPaymentInfoException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public DepositPaymentInfoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}