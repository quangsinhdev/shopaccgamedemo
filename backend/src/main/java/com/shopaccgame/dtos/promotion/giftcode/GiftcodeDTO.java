package com.shopaccgame.dtos.promotion.giftcode;

import java.time.LocalDateTime;

import com.shopaccgame.enums.promotion.PromotionStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GiftcodeDTO {
	private Long id;

	@Size(min = 4, max = 30, message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	@NotBlank(message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	private String code;

	@Min(value = 1000, message = "Giá trị Giftcode phải lớn hơn 1000đ")
	private long value;

	@Enumerated(EnumType.STRING)
	private PromotionStatus promotionStatus;

	@NotNull(message = "Mô tả của Giftcode không được bỏ trống.")
	@Pattern(regexp = "^[a-zA-Z0-9\\p{L}\\s]*$", message = "Thông tin Giftcode chỉ có thể chứa chữ cái (bao gồm tiếng Việt), số và khoảng trắng.")
	private String giftcodeInfo = "";

	@NotNull(message = "Thời điểm đăng Giftcode không được bỏ trống.")
	private LocalDateTime timeOfListing = LocalDateTime.now();

	private LocalDateTime timeOfUse;

	public GiftcodeDTO() {
	}

	public GiftcodeDTO(long value) {
		this.value = value;
	}

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

	public String getGiftcodeInfo() {
		return giftcodeInfo;
	}

	public void setGiftcodeInfo(String giftcodeInfo) {
		this.giftcodeInfo = giftcodeInfo;
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
