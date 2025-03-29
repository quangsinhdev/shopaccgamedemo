package com.shopaccgame.models.transaction.deposit;

import java.time.LocalDateTime;

import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;
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
public class CardDepositOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "Network_provider", nullable = false)
	@Enumerated(EnumType.STRING)
	private DepositCardNetworkProvider depositCardNetworkProvider;

	@Column(nullable = false)
	private String serial;
	@Column(nullable = false)
	private String code;
	@Column(nullable = false)
	private long value;

	@Column(name = "Time_of_depositing", nullable = false)
	private LocalDateTime timeOfDepositing;

	@Column(nullable = false)
	private long actuallyReceive;

	@Column(name = "Card_deposit_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private CardDepositStatus cardDepositStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String depositorUsername;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DepositCardNetworkProvider getDepositCardNetworkProvider() {
		return depositCardNetworkProvider;
	}

	public void setDepositCardNetworkProvider(DepositCardNetworkProvider depositCardNetworkProvider) {
		this.depositCardNetworkProvider = depositCardNetworkProvider;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public LocalDateTime getTimeOfDepositing() {
		return timeOfDepositing;
	}

	public void setTimeOfDepositing(LocalDateTime timeOfDepositing) {
		this.timeOfDepositing = timeOfDepositing;
	}

	public long getActuallyReceive() {
		return actuallyReceive;
	}

	public void setActuallyReceive(long actuallyReceive) {
		this.actuallyReceive = actuallyReceive;
	}

	public CardDepositStatus getCardDepositStatus() {
		return cardDepositStatus;
	}

	public void setCardDepositStatus(CardDepositStatus cardDepositStatus) {
		this.cardDepositStatus = cardDepositStatus;
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

}
