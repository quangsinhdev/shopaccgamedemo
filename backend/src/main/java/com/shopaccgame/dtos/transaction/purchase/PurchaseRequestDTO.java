package com.shopaccgame.dtos.transaction.purchase;

import com.shopaccgame.enums.gameaccount.GameAccountType;
import com.shopaccgame.validators.EnumValid;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PurchaseRequestDTO {
	@NotNull(message = "Không xác định được tài khoản Game.")
	@Min(value = 1, message = "Giao dịch không thành công. Không xác định được tài khoản Game")
	private Long accountId;

	@NotNull(message = "Thể loại Game của tài khoản không được bỏ trống.")
	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = GameAccountType.class, message = "Thể loại Game của tài khoản không hợp lệ.")
	private GameAccountType gameAccountType;

	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Voucher không được chứa khoảng trắng và ký tự đặc biệt.")
	private String voucher;

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

	public String getVoucher() {
		return voucher;
	}

	public void setVoucher(String voucher) {
		this.voucher = voucher;
	}
}