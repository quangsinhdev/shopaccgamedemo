package com.shopaccgame.models.transaction.gameaccount;

import java.time.LocalDateTime;

import com.shopaccgame.enums.gameaccount.GameAccountType;
import com.shopaccgame.models.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class GameAccountTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "account_id", nullable = false)
	private Long accountId;

	@Column(name = "game_account_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private GameAccountType gameAccountType;

	@Column(nullable = false)
	private long price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String transactorUsername;

	@Column(nullable = false)
	private LocalDateTime transactionDate;
	@Column(nullable = false)
	private String usernameAccount;
	@Column(nullable = false)
	private String passwordAccount;
	@Column(nullable = false)
	private String emailAccount;
	@Column(nullable = false)
	private String phoneNumberAccount;
	@Column(nullable = false)
	private String accountDescription;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTransactorUsername() {
		return transactorUsername;
	}

	public void setTransactorUsername(String transactorUsername) {
		this.transactorUsername = transactorUsername;
	}

	public LocalDateTime getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDateTime transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getUsernameAccount() {
		return usernameAccount;
	}

	public void setUsernameAccount(String usernameAccount) {
		this.usernameAccount = usernameAccount;
	}

	public String getPasswordAccount() {
		return passwordAccount;
	}

	public void setPasswordAccount(String passwordAccount) {
		this.passwordAccount = passwordAccount;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public void setEmailAccount(String emailAccount) {
		this.emailAccount = emailAccount;
	}

	public String getPhoneNumberAccount() {
		return phoneNumberAccount;
	}

	public void setPhoneNumberAccount(String phoneNumberAccount) {
		this.phoneNumberAccount = phoneNumberAccount;
	}

	public String getAccountDescription() {
		return accountDescription;
	}

	public void setAccountDescription(String accountDescription) {
		this.accountDescription = accountDescription;
	}

}
