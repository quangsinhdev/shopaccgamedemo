package com.shopaccgame.dtos.userauthentication;

public class JwtResponseDTO {
	private String message;
	private long lockTime;
	private String csrfToken;

	public JwtResponseDTO(String message, long lockTime, String csrfToken) {
		this.message = message;
		this.lockTime = lockTime;
		this.csrfToken = csrfToken;
	}

	public JwtResponseDTO(String message, String csrfToken) {
		this.message = message;
		this.lockTime = 0;
		this.csrfToken = csrfToken;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getLockTime() {
		return lockTime;
	}

	public void setLockTime(long lockTime) {
		this.lockTime = lockTime;
	}

	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}
}