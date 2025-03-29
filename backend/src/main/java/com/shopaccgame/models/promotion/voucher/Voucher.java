package com.shopaccgame.models.promotion.voucher;

import java.time.LocalDateTime;

import com.shopaccgame.models.promotion.Promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Voucher extends Promotion {
	@Column(nullable = false)
	private LocalDateTime voucherExpireDate;

	public LocalDateTime getVoucherExpireDate() {
		return voucherExpireDate;
	}

	public void setVoucherExpireDate(LocalDateTime voucherExpireDate) {
		this.voucherExpireDate = voucherExpireDate;
	}
}
