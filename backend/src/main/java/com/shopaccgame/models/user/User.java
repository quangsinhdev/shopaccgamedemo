package com.shopaccgame.models.user;

import java.time.LocalDateTime;
import java.util.List;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.models.transaction.deposit.CardDepositOrder;
import com.shopaccgame.models.transaction.deposit.VNPayTransaction;
import com.shopaccgame.models.transaction.gameaccount.GameAccountTransaction;
import com.shopaccgame.models.transaction.withdrawal.WithdrawalOrder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "User")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String fullname;
	@Column(nullable = false, unique = true)
	private String username;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private UserStatus userStatus;
	private String provider;
	private String providerId;
	@Column(nullable = false)
	private long balance = 0;
	@Column(nullable = false)
	private long totaldeposit = 0;
	@Column(nullable = false)
	private LocalDateTime TimeCreateAt = LocalDateTime.now();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<VNPayTransaction> vnpaytransactions;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CardDepositOrder> cardDepositOrders;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<GameAccountTransaction> accountTransactions;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<WithdrawalOrder> withdrawalOrders;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
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

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public long getTotaldeposit() {
		return totaldeposit;
	}

	public void setTotaldeposit(long totaldeposit) {
		this.totaldeposit = totaldeposit;
	}

	public LocalDateTime getTimeCreateAt() {
		return TimeCreateAt;
	}

	public void setTimeCreateAt(LocalDateTime timeCreateAt) {
		TimeCreateAt = timeCreateAt;
	}

	public List<CardDepositOrder> getCardDepositOrders() {
		return cardDepositOrders;
	}

	public void setCardDepositOrders(List<CardDepositOrder> cardDepositOrders) {
		this.cardDepositOrders = cardDepositOrders;
	}

	public List<GameAccountTransaction> getAccountTransaction() {
		return accountTransactions;
	}

	public void setAccountTransaction(List<GameAccountTransaction> accountTransactions) {
		this.accountTransactions = accountTransactions;
	}

	public List<VNPayTransaction> getVnpaytransactions() {
		return vnpaytransactions;
	}

	public void setVnpaytransactions(List<VNPayTransaction> vnpaytransactions) {
		this.vnpaytransactions = vnpaytransactions;
	}

	public List<WithdrawalOrder> getWithdrawalOrders() {
		return withdrawalOrders;
	}

	public void setWithdrawalOrders(List<WithdrawalOrder> withdrawalOrders) {
		this.withdrawalOrders = withdrawalOrders;
	}
}
