package com.shopaccgame.dtos.transaction.deposit;

public class VNPayResultDTO {
	private String orderInfo;
	private String totalPrice;
	private String paymentTime;
	private String transactionId;
	private boolean success;

	public VNPayResultDTO(String orderInfo, String totalPrice, String paymentTime, String transactionId,
			boolean success) {
		this.orderInfo = orderInfo;
		this.totalPrice = totalPrice;
		this.paymentTime = paymentTime;
		this.transactionId = transactionId;
		this.success = success;
	}

	public String getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(String orderInfo) {
		this.orderInfo = orderInfo;
	}

	public String getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(String paymentTime) {
		this.paymentTime = paymentTime;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
