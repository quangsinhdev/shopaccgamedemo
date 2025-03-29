package com.shopaccgame.services.user.authentication;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.models.user.CustomUserDetails;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginUserService implements UserDetailsService {


	private final UserRepository userRepository;
	private final LoginAttemptService loginAttemptService;

	public LoginUserService(UserRepository userRepository, LoginAttemptService loginAttemptService) {
		this.userRepository = userRepository;
		this.loginAttemptService = loginAttemptService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if (username == null || username.trim().isEmpty()) {
			throw new UsernameNotFoundException("Username không hợp lệ");
		}

		if (loginAttemptService.checkAccountLocked(username)) {
			long remainingTimeInMinutes = loginAttemptService.getLockTime(username) / 60;
			remainingTimeInMinutes = remainingTimeInMinutes < 1 ? 1 : remainingTimeInMinutes;
			throw new UsernameNotFoundException(
					"Tài khoản bị khóa do đăng nhập sai quá nhiều. Thử lại sau: " + remainingTimeInMinutes + " phút");
		}

		User user = userRepository.findByUsername(username).orElseThrow(() -> {
			return new UsernameNotFoundException("Tài khoản không tồn tại");
		});

		if (user.getRole() == null) {
			user.setRole(UserRole.USER);
		}

		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new UsernameNotFoundException("Tài khoản đã bị khóa. Liên hệ Admin để được hỗ trợ.");
		}

		return new CustomUserDetails(user);
	}
}