package com.shopaccgame.dtos.transaction.deposit;

import java.time.LocalDateTime;

public class VNPayTransactionDTO {

	private int amount;
	private String transactionId;
	private String status;
	private LocalDateTime timeOfDepositing;
	private Long userId;
	private String depositorUsername;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getTimeOfDepositing() {
		return timeOfDepositing;
	}

	public void setTimeOfDepositing(LocalDateTime timeOfDepositing) {
		this.timeOfDepositing = timeOfDepositing;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDepositorUsername() {
		return depositorUsername;
	}

	public void setDepositorUsername(String depositorUsername) {
		this.depositorUsername = depositorUsername;
	}

}