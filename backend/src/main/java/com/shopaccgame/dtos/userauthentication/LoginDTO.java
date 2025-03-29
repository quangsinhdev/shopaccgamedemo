package com.shopaccgame.dtos.userauthentication;

import com.shopaccgame.validators.NoWhitespace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginDTO {
	@NotBlank(message = "Tài khoản không được bỏ trống hoặc chứa khoảng trắng")
	@Size(min = 8, max = 50, message = "Tài khoản có độ dài tối thiểu 8 ký tự và tối đa 50 ký tự")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản chỉ có thể chứa số và chữ cái")
	@NoWhitespace(message = "Tài khoản không hợp lệ.")
	private String username;

	@NotBlank(message = "Mật khẩu không được bỏ trống hoặc chỉ là khoảng trắng")
	@Size(min = 8, max = 100, message = "Mật khẩu có độ dài tối thiểu là 8 ký tự và tối đa 100 ký tự")
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số.")
	@NoWhitespace(message = "Mật khẩu không hợp lệ")
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
