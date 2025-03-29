package com.shopaccgame.exceptions.promotion.voucher;

import org.springframework.http.HttpStatus;

public class VoucherNotAvailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public VoucherNotAvailableException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public VoucherNotAvailableException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}