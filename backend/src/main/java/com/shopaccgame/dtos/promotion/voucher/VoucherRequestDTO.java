package com.shopaccgame.dtos.promotion.voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VoucherRequestDTO {
	@Size(min = 4, max = 30, message = "Mã Voucher không đúng. Vui lòng kiểm tra lại.")
	@NotBlank(message = "Mã Voucher không đúng. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Mã Voucher không đúng. Vui lòng kiểm tra lại.")
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
