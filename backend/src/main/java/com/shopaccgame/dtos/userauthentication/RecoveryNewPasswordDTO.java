package com.shopaccgame.dtos.userauthentication;

import jakarta.validation.constraints.Pattern;

import com.shopaccgame.validators.NoWhitespace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecoveryNewPasswordDTO {
	@NotBlank(message = "Token không được bỏ trống hoặc chỉ là khoảng trắng")
	private String recoveryToken;
	
	@NotBlank(message = "Username không được bỏ trống hoặc chỉ là khoảng trắng")
	private String username;

	@NotBlank(message = "Mật khẩu không được bỏ trống hoặc chỉ là khoảng trắng")
	@Size(min = 8, max = 100, message = "Mật khẩu có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự")
	@NoWhitespace(message = "Mật khẩu không được chứa khoảng trắng")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số.")
	private String newPassword;

	@NotBlank(message = "Mật khẩu không được bỏ trống hoặc chỉ là khoảng trắng")
	@Size(min = 8, max = 100, message = "Mật khẩu có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số.")
	private String confirmNewPassword;

	public String getRecoveryToken() {
		return recoveryToken;
	}

	public void setRecoveryToken(String recoveryToken) {
		this.recoveryToken = recoveryToken;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
