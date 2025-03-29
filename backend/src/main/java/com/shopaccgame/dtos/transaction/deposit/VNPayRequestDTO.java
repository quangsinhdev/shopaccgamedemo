package com.shopaccgame.dtos.transaction.deposit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VNPayRequestDTO {

	@Min(value = 10000, message = "Số tiền cần nạp tối thiểu là 10.000đ")
	@Max(value = 2000000000, message = "Số tiền cần nạp tối đa là 2.000.000.000đ")
	private int amount;

	@NotBlank(message = "Mô tả nạp tiền VNPay không được bỏ trống.")
	@Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Mô tả đơn hàng nạp tiền VNPay không đúng")
	private String orderInfo;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(String orderInfo) {
		this.orderInfo = orderInfo;
	}

}