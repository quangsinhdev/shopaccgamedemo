package com.shopaccgame.dtos.userauthentication;

import com.shopaccgame.validators.NoWhitespace;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RecoveryDTO {

	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tài khoản chỉ có thể chứa số và chữ cái")
	@NotBlank(message = "Tài khoản không được bỏ trống hoặc chỉ chứa khoảng trắng")
	@Size(min = 8, max = 50, message = "Tài khoản có độ dài tối thiểu 8 ký tự và tối đa 50 ký tự")
	@NoWhitespace(message = "Tài khoản không hợp lệ! (Không được chứa khoảng trắng)")
	private String username;

	@Pattern(regexp = "^[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z]{2,}$", message = "Email không hợp lệ")
	@Email(message = "Email không hợp lệ. Vui lòng thử lại")
	@NotBlank(message = "Email không được bỏ trống hoặc chỉ chứa khoảng trắng")
	@Size(min = 6, max = 80, message = "Email không hợp lệ. Vui lòng thử lại")
	@NoWhitespace(message = "Email không hợp lệ.")
	private String email;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
