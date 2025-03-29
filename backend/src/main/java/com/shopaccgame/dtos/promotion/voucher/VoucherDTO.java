package com.shopaccgame.dtos.promotion.voucher;

import java.time.LocalDateTime;

import com.shopaccgame.enums.promotion.PromotionStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VoucherDTO {
	private Long id;

	@Size(min = 4, max = 30, message = "Mã Voucher không đúng. Vui lòng kiểm tra lại.")
	@NotNull(message = "Mã Voucher không thể bỏ trống. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Mã Voucher không đúng. Vui lòng kiểm tra lại.")
	private String code = "";

	@Min(value = 1000, message = "Giá trị Voucher phải lớn hơn 1000đ")
	private long value;

	@Enumerated(EnumType.STRING)
	private PromotionStatus promotionStatus;

	@NotNull(message = "Ngày hết hạn của Voucher không được bỏ trống")
	@FutureOrPresent(message = "Ngày hết hạn của Voucher không thể là quá khứ.")
	private LocalDateTime voucherExpireDate = LocalDateTime.now().plusDays(1);

	@NotNull(message = "Thời điểm đăng Voucher không được bỏ trống.")
	private LocalDateTime timeOfListing = LocalDateTime.now();

	private LocalDateTime timeOfUse;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public PromotionStatus getPromotionStatus() {
		return promotionStatus;
	}

	public void setPromotionStatus(PromotionStatus promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public LocalDateTime getVoucherExpireDate() {
		return voucherExpireDate;
	}

	public void setVoucherExpireDate(LocalDateTime voucherExpireDate) {
		this.voucherExpireDate = voucherExpireDate;
	}

	public LocalDateTime getTimeOfListing() {
		return timeOfListing;
	}

	public void setTimeOfListing(LocalDateTime timeOfListing) {
		this.timeOfListing = timeOfListing;
	}

	public LocalDateTime getTimeOfUse() {
		return timeOfUse;
	}

	public void setTimeOfUse(LocalDateTime timeOfUse) {
		this.timeOfUse = timeOfUse;
	}

}
