package com.shopaccgame.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.shopaccgame.dtos.userauthentication.JwtResponseDTO;
import com.shopaccgame.dtos.userauthentication.LoginDTO;
import com.shopaccgame.dtos.userauthentication.RecoveryDTO;
import com.shopaccgame.dtos.userauthentication.RecoveryNewPasswordDTO;
import com.shopaccgame.dtos.userauthentication.RegisterDTO;
import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.exceptions.user.authentication.LoginException;
import com.shopaccgame.exceptions.user.authentication.LogoutException;
import com.shopaccgame.exceptions.user.authentication.PasswordRecoverException;
import com.shopaccgame.exceptions.user.authentication.RefreshTokenException;
import com.shopaccgame.exceptions.user.authentication.VerifyUsernameAndRecoveryTokenException;
import com.shopaccgame.models.user.User;
import com.shopaccgame.security.jwt.JwtTokenProvider;
import com.shopaccgame.services.user.authentication.RecoveryService;
import com.shopaccgame.services.user.authentication.RegisterUserService;
import com.shopaccgame.utils.AuthenticationUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication API", description = "APIs related to User Authentication")
public class AuthenticationController {
	private final RegisterUserService registerUserService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final RecoveryService recoveryService;
	private final RedisTemplate<String, Object> redisTemplate;

	public AuthenticationController(RegisterUserService registerUserService,
			AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
			RecoveryService recoveryService, RedisTemplate<String, Object> redisTemplate) {
		this.registerUserService = registerUserService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
		this.recoveryService = recoveryService;
		this.redisTemplate = redisTemplate;
	}

	@Operation(summary = "Verify recovery token match username", description = "Verify recovery token match username")
	@GetMapping("/verify-recovery-token")
	public ResponseEntity<Boolean> verifyUsernameAndRecoveryToken(@RequestParam("username") String username,
			@RequestParam("recoveryToken") String recoveryToken) {
		if (username == null || username.trim().isEmpty()) {
			throw new VerifyUsernameAndRecoveryTokenException("Username không được để trống.", HttpStatus.BAD_REQUEST);
		}
		if (recoveryToken == null || recoveryToken.trim().isEmpty()) {
			throw new VerifyUsernameAndRecoveryTokenException("Recovery token không được để trống.",
					HttpStatus.BAD_REQUEST);
		}
		return recoveryService.verifyUsernameAndRecoveryTokenForResponse(username, recoveryToken)
				? ResponseEntity.ok(true)
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
	}
	
	@Operation(summary = "User login", description = "User login using form login")
	@PostMapping("/login")
	public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request,
			HttpServletResponse response) {
		String username = loginDTO.getUsername();
		String password = loginDTO.getPassword();

		if (username == null || username.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JwtResponseDTO("Username không hợp lệ", 0, null));
		}
		if (password == null || password.trim().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new JwtResponseDTO("Password không hợp lệ", 0, null));
		}

		request.setAttribute("loginUsername", username);

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);

		String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole());
		String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole());
		try {
			redisTemplate.opsForValue().set(refreshToken, user.getUsername(),
					jwtTokenProvider.getRefreshTokenExpiration() / 1000, TimeUnit.SECONDS);
		} catch (Exception ex) {
			throw new LoginException("Đã xảy ra lỗi khi truy xuất dữ liệu từ Redis", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Cookie accessCookie = new Cookie("accessToken", accessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
		accessCookie.setAttribute("SameSite", "None");
		response.addCookie(accessCookie);

		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/api/users/refresh-token");
		refreshCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
		refreshCookie.setAttribute("SameSite", "None");
		response.addCookie(refreshCookie);

		String csrfToken = UUID.randomUUID().toString();
		try {
			redisTemplate.opsForValue().set("csrf:" + user.getUsername(), csrfToken,
					jwtTokenProvider.getAccessTokenExpiration() / 1000, TimeUnit.SECONDS);
		} catch (Exception ex) {
			throw new LoginException("Đã xảy ra lỗi khi truy xuất dữ liệu Username từ Redis",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.ok(new JwtResponseDTO("Đăng nhập thành công.", csrfToken));
	}

	@Operation(summary = "User logout", description = "User logout and deletes necessary information")
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = null;
		String username = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
					username = (String) redisTemplate.opsForValue().get(refreshToken);
					break;
				}
			}
		}

		try {

			if (refreshToken != null) {
				redisTemplate.delete(refreshToken);
			}

			if (username != null) {
				redisTemplate.delete("csrf:" + username);
			}
		} catch (Exception ex) {
			throw new LogoutException("Đã xảy ra lỗi khi đăng xuất", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Cookie accessCookie = new Cookie("accessToken", "");
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge(0);
		accessCookie.setAttribute("SameSite", "None");
		response.addCookie(accessCookie);

		Cookie refreshCookie = new Cookie("refreshToken", "");
		refreshCookie.setHttpOnly(true);
		refreshCookie.setSecure(true);
		refreshCookie.setPath("/api/users/refresh-token");
		refreshCookie.setMaxAge(0);
		refreshCookie.setAttribute("SameSite", "None");
		response.addCookie(refreshCookie);

		return ResponseEntity.ok("Logout successful");
	}

	@Operation(summary = "Refresh access token and CSRF Token", description = "Renew access token and CSRF token upon expiration")
	@PostMapping("/refresh-token")
	public ResponseEntity<JwtResponseDTO> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("refreshToken".equals(cookie.getName())) {
					refreshToken = cookie.getValue();
					break;
				}
			}
		}

		if (refreshToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JwtResponseDTO("Không tìm thấy Refresh Token.", 0, null));
		}

		String username = (String) redisTemplate.opsForValue().get(refreshToken);
		if (username == null || jwtTokenProvider.isTokenExpired(refreshToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new JwtResponseDTO("Thông tin xác thực không hợp lệ hoặc Refresh token hết hạn.", 0, null));
		}

		UserRole role = jwtTokenProvider.getRoleFromToken(refreshToken);
		String newAccessToken = jwtTokenProvider.generateAccessToken(username, role);

		Cookie accessCookie = new Cookie("accessToken", newAccessToken);
		accessCookie.setHttpOnly(true);
		accessCookie.setSecure(true);
		accessCookie.setPath("/");
		accessCookie.setMaxAge((int) (jwtTokenProvider.getAccessTokenExpiration() / 1000));
		accessCookie.setAttribute("SameSite", "None");
		response.addCookie(accessCookie);

		String newCsrfToken = UUID.randomUUID().toString();
		try {
			redisTemplate.opsForValue().set("csrf:" + username, newCsrfToken,
					jwtTokenProvider.getAccessTokenExpiration() / 1000, TimeUnit.SECONDS);
		} catch (Exception ex) {
			throw new RefreshTokenException("Đã xảy ra lỗi khi làm mới Authentication Token.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ResponseEntity.ok(new JwtResponseDTO("Token refreshed", newCsrfToken));
	}

	@Operation(summary = "Register a new user account", description = "Register a new user account on the system")
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> registerUserAccount(@Valid @RequestBody RegisterDTO registerDTO) {
		Map<String, Object> response = new HashMap<>();
		registerUserService.registerUserWithLock(registerDTO);
		response.put("success", true);
		response.put("successMessage", "Đăng ký tài khoản thành công! Chuyển về trang đăng nhập trong vài giây...");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Submit a password recovery request", description = "Submit a request to restore the password of a user account on the system")
	@PostMapping("/password-recovery")
	public ResponseEntity<Map<String, Object>> requestRecoveryPassword(@Valid @RequestBody RecoveryDTO recoveryDTO,
			HttpServletRequest request) throws MessagingException {
		Map<String, Object> response = new HashMap<>();

		recoveryService.sendRecoveryMail(recoveryDTO.getUsername(), recoveryDTO.getEmail(), request);

		response.put("status", "success");
		response.put("message", "Thông tin khôi phục đã được gửi đến email: " + recoveryDTO.getEmail());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Update new password after successfully authenticating password recovery OTP", description = "Update the new password of the user account after successfully authenticating the password recovery OTP")
	@PatchMapping("/password-recovery")
	public ResponseEntity<Map<String, Object>> updateRecoveryNewPassword(
			@Valid @RequestBody RecoveryNewPasswordDTO recoveryNewPasswordDTO) {
		String recoveryToken = recoveryNewPasswordDTO.getRecoveryToken();
		String username = recoveryNewPasswordDTO.getUsername();
		String newPassword = recoveryNewPasswordDTO.getNewPassword();
		String confirmNewPassword = recoveryNewPasswordDTO.getConfirmNewPassword();

		Map<String, Object> response = new HashMap<>();

		if (username == null || username.trim().isEmpty()) {
			throw new PasswordRecoverException("Username không được để trống.", HttpStatus.BAD_REQUEST);
		}
		if (recoveryToken == null || recoveryToken.trim().isEmpty()) {
			throw new PasswordRecoverException("Recovery token không được để trống.", HttpStatus.BAD_REQUEST);
		}
		if (newPassword == null || newPassword.trim().isEmpty()) {
			throw new PasswordRecoverException("Mật khẩu mới không được để trống.", HttpStatus.BAD_REQUEST);
		}
		if (confirmNewPassword == null || confirmNewPassword.trim().isEmpty()) {
			throw new PasswordRecoverException("Mật khẩu xác nhận không được để trống.", HttpStatus.BAD_REQUEST);
		}

		recoveryService.verifyUsernameAndRecoveryToken(username, recoveryToken);

		if (username.equals(newPassword)) {
			throw new PasswordRecoverException("Vì lý do bảo mật: Tài khoản và mật khẩu không được trùng khớp.",
					HttpStatus.BAD_REQUEST);
		}

		if (!recoveryService.checkConfirmNewPassword(newPassword, confirmNewPassword)) {
			throw new PasswordRecoverException("Mật khẩu mới và mật khẩu xác nhận chưa trùng khớp.",
					HttpStatus.BAD_REQUEST);
		}

		if (!recoveryService.checkNewPasswordAndCurrentPassword(recoveryToken, newPassword)) {
			throw new PasswordRecoverException("Mật khẩu mới và mật khẩu hiện tại không được trùng nhau.",
					HttpStatus.BAD_REQUEST);
		}

		recoveryService.UpdatePassword(username, recoveryToken, newPassword);

		response.put("message", "Đã cập nhật mật khẩu mới thành công. Vui lòng đợi vài giây...");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}