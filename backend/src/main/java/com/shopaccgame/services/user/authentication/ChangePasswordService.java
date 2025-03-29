package com.shopaccgame.services.user.authentication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.exceptions.user.UserNotFoundException;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ChangePasswordService {
	private final UserRepository userRepository;
	private PasswordEncoder passwordEncoder;

	public ChangePasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public boolean CheckCurrentPassword(String currentPasswordToChange, String currentPassword) {
		return passwordEncoder.matches(currentPasswordToChange, currentPassword);
	}

	public boolean CheckCurrentPasswordAndNewPassword(String newPassword, String currentPassword) {
		return passwordEncoder.matches(newPassword, currentPassword);
	}

	public boolean CheckConfirmNewPassword(String newPassword, String confirmNewPassword) {
		return (newPassword.equals(confirmNewPassword));
	}

	@Transactional
	public void UpdateNewPassword(String username, String newPassword) {
		User user = userRepository.findByUsername(username).orElseThrow(()-> new UserNotFoundException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));
		user.setPassword(passwordEncoder.encode(newPassword));
	}
}
