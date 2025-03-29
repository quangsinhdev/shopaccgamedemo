package com.shopaccgame.dtos.transaction.deposit;

import java.time.LocalDateTime;

import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class CardDepositTransactionDTO {

	private Long id;

	@Enumerated(EnumType.STRING)
	private DepositCardNetworkProvider depositCardNetworkProvider;

	private String serial;
	private String code;
	private long value;
	private LocalDateTime timeOfDepositing;
	private long actuallyReceive;
	@Enumerated(EnumType.STRING)
	private CardDepositStatus cardDepositStatus;
	private Long userId;
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
