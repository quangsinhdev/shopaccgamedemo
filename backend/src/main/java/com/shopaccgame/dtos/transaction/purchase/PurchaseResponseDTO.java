package com.shopaccgame.dtos.transaction.purchase;

import java.time.LocalDateTime;

import com.shopaccgame.dtos.user.UserDTO;
import com.shopaccgame.enums.gameaccount.GameAccountType;

public class PurchaseResponseDTO {
	private Long accountId;
	private GameAccountType gameAccountType;
	private long price;
	private UserDTO user;
	private LocalDateTime transactionDate;

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

	public UserDTO getUser() {
		return user;
	}

	public void setUser(UserDTO user) {
		this.user = user;
	}

	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}
}
