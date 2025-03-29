package com.shopaccgame.dtos.userauthentication;

import com.shopaccgame.validators.NoWhitespace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {
	@NotBlank(message = "Mật khẩu hiện tại không đúng. Vui lòng kiểm tra lại!")
	@Size(min = 8, max = 100, message = "Mật khẩu hiện tại không đúng. Vui lòng kiểm tra lại!")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu hiện tại không đúng. Vui lòng kiểm tra lại!")
	@NoWhitespace(message = "Mật khẩu hiện tại không đúng.")
	private String currentPassword;

	@NotBlank(message = "Mật khẩu mới không được bỏ trống hoặc chỉ là khoảng trắng")
	@Size(min = 8, max = 100, message = "Mật khẩu mới có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu mới phải chứa ít nhất một chữ cái và một chữ số.")
	@NoWhitespace(message = "Mật khẩu mới không được chứa khoảng trắng")
	private String newPassword;

	@NotBlank(message = "Mật khẩu xác nhận không được bỏ trống hoặc chỉ là khoảng trắng")
	@Size(min = 8, max = 100, message = "Mật khẩu xác nhận có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu xác nhận phải chứa ít nhất một chữ cái và một chữ số.")
	private String confirmNewPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}

}
