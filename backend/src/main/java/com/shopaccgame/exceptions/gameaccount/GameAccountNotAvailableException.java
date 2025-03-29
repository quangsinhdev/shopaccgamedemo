package com.shopaccgame.exceptions.gameaccount;

import org.springframework.http.HttpStatus;

public class GameAccountNotAvailableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final HttpStatus status;

	public GameAccountNotAvailableException(String message) {
		super(message);
		this.status = HttpStatus.BAD_REQUEST;
	}

	public GameAccountNotAvailableException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}