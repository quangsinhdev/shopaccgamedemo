package com.shopaccgame.exceptions.user.authentication;

import org.springframework.http.HttpStatus;

public class TokenAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final HttpStatus status;

    public TokenAuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }

    public TokenAuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}