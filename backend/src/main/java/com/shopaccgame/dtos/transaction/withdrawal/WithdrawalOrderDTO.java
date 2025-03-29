package com.shopaccgame.dtos.transaction.withdrawal;

import com.shopaccgame.enums.withdrawal.WithdrawalMethod;
import com.shopaccgame.enums.withdrawal.WithdrawalStatus;
import com.shopaccgame.validators.EnumValid;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class WithdrawalOrderDTO {
	private Long id;

	@NotNull(message = "Hình thức rút tiền không được bỏ trống.")
	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = WithdrawalMethod.class, message = "Hình thức rút tiền không hợp lệ.")
	private WithdrawalMethod withdrawalMethod;

	@Min(value = 100000, message = "Số tiền rút tối thiểu là 100000đ")
	private long amount;

	@NotNull(message = "Trạng thái rút tiền không được bỏ trống.")
	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = WithdrawalStatus.class, message = "Trạng thái rút tiền không hợp lệ.")
	private WithdrawalStatus withdrawStatus = WithdrawalStatus.PENDING;

	@NotBlank(message = "Mô tả rút tiền không được bỏ trống.")
	@Pattern(regexp = "^[a-zA-Zàáảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ0-9,.\\s]+$", message = "Mô tả chỉ được bao gồm số, chữ cái, dấu . và ,")
	private String withdrawDescription = "";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public WithdrawalMethod getWithdrawalMethod() {
		return withdrawalMethod;
	}

	public void setWithdrawalMethod(WithdrawalMethod withdrawalMethod) {
		this.withdrawalMethod = withdrawalMethod;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public WithdrawalStatus getWithdrawStatus() {
		return withdrawStatus;
	}

	public void setWithdrawStatus(WithdrawalStatus withdrawStatus) {
		this.withdrawStatus = withdrawStatus;
	}

	public String getWithdrawDescription() {
		return withdrawDescription;
	}

	public void setWithdrawDescription(String withdrawDescription) {
		this.withdrawDescription = withdrawDescription;
	}
}
