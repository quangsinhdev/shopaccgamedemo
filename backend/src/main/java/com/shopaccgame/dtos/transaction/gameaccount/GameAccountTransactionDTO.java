package com.shopaccgame.dtos.transaction.gameaccount;

import java.time.LocalDateTime;

import com.shopaccgame.enums.gameaccount.GameAccountType;

public class GameAccountTransactionDTO {

	private Long accountId;

	private GameAccountType gameAccountType;

	private long price;

	private LocalDateTime transactionDate;

	private Long userId;

	private String transactorUsername;

	private String username;

	private String password;

	private String email;

	private String phone;

	private String description;

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public GameAccountType getGameAccountType() {
		return gameAccountType;
	}

	public void setGameAccountType(GameAccountType gameAccountType) {
		this.gameAccountType = gameAccountType;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTransactorUsername() {
		return transactorUsername;
	}

	public void setTransactorUsername(String transactorUsername) {
		this.transactorUsername = transactorUsername;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
