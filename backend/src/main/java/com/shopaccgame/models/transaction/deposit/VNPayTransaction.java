package com.shopaccgame.models.transaction.deposit;

import java.time.LocalDateTime;

import com.shopaccgame.models.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class VNPayTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String depositorUsername;

	@Column(nullable = false)
	private Long amount;

	@Column(name = "transaction_id", nullable = false)
	private String transactionId;

	@Column(name = "txn_ref", nullable = false)
	private String txnRef;

	@Column(nullable = false)
	private String status;

	@Column(name = "time_of_depositing", nullable = false)
	private LocalDateTime timeOfDepositing;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDepositorUsername() {
		return depositorUsername;
	}

	public void setDepositorUsername(String depositorUsername) {
		this.depositorUsername = depositorUsername;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTxnRef() {
		return txnRef;
	}

	public void setTxnRef(String txnRef) {
		this.txnRef = txnRef;
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

}
