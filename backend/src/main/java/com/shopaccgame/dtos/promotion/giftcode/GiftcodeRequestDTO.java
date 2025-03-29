package com.shopaccgame.dtos.promotion.giftcode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GiftcodeRequestDTO {

	@Size(min = 4, max = 30, message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	@NotBlank(message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Mã Giftcode không đúng. Vui lòng kiểm tra lại.")
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
