package com.shopaccgame.dtos.transaction.deposit;

import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;
import com.shopaccgame.validators.EnumValid;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CardDepositRequestDTO {

	@NotNull(message = "Loại thẻ cào không hợp lệ")
	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = DepositCardNetworkProvider.class, message = "Nhà cung cấp thẻ cào không hợp lệ.")
	private DepositCardNetworkProvider depositCardNetworkProvider;

	@NotBlank(message = "Serial thẻ không được bỏ trống")
	@Size(min = 5, max = 20, message = "Serial thẻ cào không đúng. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Serial thẻ chỉ được phép chứa chữ cái và số, không chứa khoảng trắng.")
	private String serial = "";

	@NotBlank(message = "Mã thẻ không được bỏ trống")
	@Size(min = 5, max = 20, message = "Mã thẻ không đúng. Vui lòng kiểm tra lại.")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Mã thẻ chỉ được phép chứa chữ cái và số, không chứa khoảng trắng.")
	private String code = "";

	@Min(value = 10000, message = "Mệnh giá thẻ cào phải lớn hơn 10.000")
	@Max(value = 2000000, message = "Mệnh giá thẻ cào không được lớn hơn 2.000.000")
	private long value;

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

}
