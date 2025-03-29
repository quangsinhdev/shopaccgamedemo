package com.shopaccgame.exceptions.promotion.giftcode;

import org.springframework.http.HttpStatus;

public class GiftcodeNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public GiftcodeNotFoundException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }

    public GiftcodeNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}