package com.shopaccgame.services.user.authentication;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;
import com.shopaccgame.dtos.userauthentication.RegisterDTO;
import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.exceptions.user.authentication.RegisterException;

import java.util.concurrent.TimeUnit;

@Service
public class RegisterUserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisTemplate<String, Object> redisTemplate;

	public RegisterUserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			RedisTemplate<String, Object> redisTemplate) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.redisTemplate = redisTemplate;
	}

	public User register(User user) {
		return userRepository.save(user);
	}

	public boolean checkUsernameValid(String username) {
		return userRepository.existsByUsername(username);
	}

	public boolean checkEmailValid(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean UsernameAndPasswordMatch(String username, String password) {
		return username.equals(password);
	}

	public boolean validatePassword(String password, String confirmPassword) {
		return password.equals(confirmPassword);
	}

	@Transactional
	public User registerUser(RegisterDTO registerDTO) {
		User user = new User();
		String encodePassword = passwordEncoder.encode(registerDTO.getPassword());
		user.setFullname(registerDTO.getFullname());
		user.setEmail(registerDTO.getEmail());
		user.setUsername(registerDTO.getUsername());
		user.setPassword(encodePassword);
		user.setBalance(0);
		user.setTotaldeposit(0);
		user.setUserStatus(UserStatus.ACTIVE);
		user.setRole(UserRole.USER);
		return userRepository.save(user);
	}

	@Transactional
	public User registerUserWithLock(RegisterDTO registerDTO) {

		String usernameLockKey = "lock:username:" + registerDTO.getUsername();
		String emailLockKey = "lock:email:" + registerDTO.getEmail();

		Boolean usernameLocked = redisTemplate.opsForValue().setIfAbsent(usernameLockKey, "locked", 10,
				TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(usernameLocked)) {
			throw new RegisterException("Tên tài khoản đang được xử lý, thử lại sau!", HttpStatus.CONFLICT);
		}

		Boolean emailLocked = redisTemplate.opsForValue().setIfAbsent(emailLockKey, "locked", 10, TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(emailLocked)) {
			redisTemplate.delete(usernameLockKey);
			throw new RegisterException("Email đang được xử lý, thử lại sau!", HttpStatus.CONFLICT);
		}

		try {
			if (checkUsernameValid(registerDTO.getUsername())) {
				throw new RegisterException("Tên tài khoản đã tồn tại! Hãy thử lại", HttpStatus.CONFLICT);
			}

			if (checkEmailValid(registerDTO.getEmail())) {
				throw new RegisterException("Email đã tồn tại! Hãy thử lại email khác", HttpStatus.CONFLICT);
			}

			if (UsernameAndPasswordMatch(registerDTO.getUsername(), registerDTO.getPassword())) {
				throw new RegisterException("Vì lý do bảo mật, mật khẩu không được trùng với tài khoản!",
						HttpStatus.BAD_REQUEST);
			}

			if (!validatePassword(registerDTO.getPassword(), registerDTO.getConfirmPassword())) {
				throw new RegisterException("Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST);
			}

			User user = registerUser(registerDTO);
			return user;

		} finally {
			redisTemplate.delete(usernameLockKey);
			redisTemplate.delete(emailLockKey);
		}
	}
}