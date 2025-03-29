package com.shopaccgame.dtos.transaction.deposit;

public class VNPayResponseDTO {
	private String paymentUrl;

	public VNPayResponseDTO(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}
}