package com.shopaccgame.exceptions.promotion.giftcode;

import org.springframework.http.HttpStatus;

public class GiftcodeNotAvailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public GiftcodeNotAvailableException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public GiftcodeNotAvailableException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}