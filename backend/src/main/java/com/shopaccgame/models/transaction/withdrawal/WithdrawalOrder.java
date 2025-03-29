package com.shopaccgame.models.transaction.withdrawal;

import java.time.LocalDateTime;

import com.shopaccgame.enums.withdrawal.WithdrawalMethod;
import com.shopaccgame.enums.withdrawal.WithdrawalStatus;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "Withdrawal_Order")
public class WithdrawalOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "Withdrawal_method", nullable = false)
	private WithdrawalMethod withdrawalMethod;

	@Column(nullable = false)
	private long amount;

	@Column(name = "Withdrawal_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private WithdrawalStatus withdrawStatus;

	@Column(nullable = false)
	private String withdrawDescription = "";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "Time_of_depositing", nullable = false)
	private LocalDateTime timeOfDepositing;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WithdrawalMethod getWithdrawalMethod() {
		return withdrawalMethod;
	}

	public void setWithdrawalMethod(WithdrawalMethod withdrawalMethod) {
		this.withdrawalMethod = withdrawalMethod;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public WithdrawalStatus getWithdrawStatus() {
		return withdrawStatus;
	}

	public void setWithdrawStatus(WithdrawalStatus withdrawStatus) {
		this.withdrawStatus = withdrawStatus;
	}

	public String getWithdrawDescription() {
		return withdrawDescription;
	}

	public void setWithdrawDescription(String withdrawDescription) {
		this.withdrawDescription = withdrawDescription;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getTimeOfDepositing() {
		return timeOfDepositing;
	}

	public void setTimeOfDepositing(LocalDateTime timeOfDepositing) {
		this.timeOfDepositing = timeOfDepositing;
	}

}
