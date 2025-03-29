package com.shopaccgame.exceptions.promotion.voucher;

import org.springframework.http.HttpStatus;

public class VoucherNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public VoucherNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public VoucherNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}