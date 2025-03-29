package com.shopaccgame.dtos.transaction.deposit;

import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class CardDepositResponseDTO {

	@Enumerated(EnumType.STRING)
	private DepositCardNetworkProvider depositCardNetworkProvider;

	private String serial;
	private String code;
	private long value;

	@Enumerated(EnumType.STRING)
	private CardDepositStatus cardDepositStatus;

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

	public CardDepositStatus getCardDepositStatus() {
		return cardDepositStatus;
	}

	public void setCardDepositStatus(CardDepositStatus cardDepositStatus) {
		this.cardDepositStatus = cardDepositStatus;
	}
}
