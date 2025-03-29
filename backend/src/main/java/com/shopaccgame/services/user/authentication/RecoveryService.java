package com.shopaccgame.services.user.authentication;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.exceptions.common.CustomNoSuchElementException;
import com.shopaccgame.exceptions.user.authentication.PasswordRecoverException;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RecoveryService {
	private final UserRepository userRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final JavaMailSender mailSender;
	private final PasswordEncoder passwordEncoder;
	private final LoginAttemptService loginAttemptService;
	private static final String REDIS_TOKEN_PREFIX = "recovery:token:";

	@Value("${recovery.token.expire.time:60}")
	private long recoveryTokenExpireTimeInMinutes;

	private long TOKEN_EXPIRY_TIME_IN_MILLIS;

	public RecoveryService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate,
			LoginAttemptService loginAttemptService, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.redisTemplate = redisTemplate;
		this.loginAttemptService = loginAttemptService;
		this.mailSender = mailSender;
		this.passwordEncoder = passwordEncoder;

		if (recoveryTokenExpireTimeInMinutes <= 0) {
			this.recoveryTokenExpireTimeInMinutes = 60;
		}
		this.TOKEN_EXPIRY_TIME_IN_MILLIS = recoveryTokenExpireTimeInMinutes * 60 * 1000;
	}

	private User getUserByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(
				() -> new PasswordRecoverException("Tài khoản không tồn tại: " + username, HttpStatus.NOT_FOUND));
	}

	private String createTokenForUser(String username) {
		String tokenCreate;
		int maxAttempts = 5;
		int attempt = 0;

		do {
			tokenCreate = generateRandomToken();
			attempt++;
			if (attempt > maxAttempts) {
				throw new PasswordRecoverException("Không thể khởi tạo token khôi phục duy nhất.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} while (Boolean.TRUE.equals(redisTemplate.hasKey(REDIS_TOKEN_PREFIX + tokenCreate)));

		redisTemplate.opsForValue().set(REDIS_TOKEN_PREFIX + tokenCreate, username, TOKEN_EXPIRY_TIME_IN_MILLIS,
				TimeUnit.MILLISECONDS);
		return tokenCreate;
	}

	private String buildRecoveryURL(String username, String tokenCreate) {
		return "https://localhost:3000/pages/client/updatepassword.html?username=" + username + "&recoveryToken="
				+ tokenCreate;
	}

	private String buildEmailContent(String recoveryURL, String userAgent, String timerequest) {
		Date expireTime = new Date(System.currentTimeMillis() + TOKEN_EXPIRY_TIME_IN_MILLIS);
		String TimeTokenExpire = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(expireTime);

		String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
				+ "<h2 style='color: #4CAF50;'>Khôi phục mật khẩu</h2>"
				+ "<p style='font-size: 16px;'>Để khôi phục mật khẩu của bạn, vui lòng nhấp vào liên kết sau:</p>"
				+ "<p><a href=\"" + recoveryURL
				+ "\" style='color: #fff; background-color: #9FB6CD; padding: 15px 30px; text-decoration: none; border-radius: 5px;'>Khôi phục mật khẩu</a></p>"
				+ "<p style='font-size: 14px;'>Yêu cầu này được gửi từ thiết bị: " + getDevice(userAgent) + "</p>"
				+ "<p style='font-size: 14px;'>Trình duyệt: " + getBrowser(userAgent) + "</p>"
				+ "<p style='font-size: 14px;'>Hệ điều hành: " + getOS(userAgent) + "</p>"
				+ "<p style='font-size: 14px;'>Yêu cầu được gửi vào lúc: " + timerequest + "</p>"
				+ "<p style='font-size: 14px;'>Thời điểm hết hạn của liên kết khôi phục là: " + TimeTokenExpire + "</p>"
				+ "<p style='font-size: 14px;'>Vui lòng không chia sẻ liên kết này với người khác.</p>"
				+ "</body></html>";

		return htmlContent;
	}

	public void sendMail(String to, String subject, String text) throws MessagingException {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(text, true);
		mailSender.send(mimeMessage);
	}

	@Transactional
	public void sendRecoveryMail(String username, String email, HttpServletRequest httpServletRequest)
			throws MessagingException {
		String userAgent = httpServletRequest.getHeader("User-Agent");
		String timerequest = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));

		User user = getUserByUsername(username);
		if (!user.getEmail().equals(email)) {
			throw new PasswordRecoverException("Email không khớp với tài khoản này.", HttpStatus.BAD_REQUEST);
		}

		String tokenCreate = createTokenForUser(username);

		String recoveryURL = buildRecoveryURL(username, tokenCreate);
		String emailContent = buildEmailContent(recoveryURL, userAgent, timerequest);

		sendMail(email, "Khôi phục mật khẩu tại Notenhanh", emailContent);
	}

	@Transactional
	private boolean updatePasswordInTransaction(String username, String newPassword) {
		try {
			Optional<User> userOptional = userRepository.findByUsername(username);
			if (userOptional.isEmpty()) {
				throw new PasswordRecoverException("Không tìm thấy người dùng với username này.", HttpStatus.NOT_FOUND);
			}

			User user = userOptional.get();
			user.setPassword(passwordEncoder.encode(newPassword));
			loginAttemptService.resetLoginAttempts(user.getUsername());
			return true;
		} catch (Exception ex) {
			throw new PasswordRecoverException("Lỗi không xác định khi cập nhật mật khẩu: " + ex.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public boolean UpdatePassword(String username, String recoveryToken, String newPassword) {
		String storedUsername = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + recoveryToken);
		if (storedUsername == null) {
			throw new PasswordRecoverException("Token không tồn tại hoặc đã hết hạn.", HttpStatus.BAD_REQUEST);
		}
		if (!storedUsername.equals(username)) {
			throw new PasswordRecoverException("Token không khớp với tài khoản này.", HttpStatus.BAD_REQUEST);
		}

		boolean success = updatePasswordInTransaction(username, newPassword);

		if (success) {
			redisTemplate.delete(REDIS_TOKEN_PREFIX + recoveryToken);
		}

		return success;
	}

	public boolean checkNewPasswordAndCurrentPassword(String recoveryToken, String newPassword) {
		String username = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + recoveryToken);
		if (username == null) {
			throw new CustomNoSuchElementException("Token khôi phục không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST);
		}

		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new CustomNoSuchElementException("Không tìm thấy người dùng với username này", HttpStatus.NOT_FOUND));
		return !passwordEncoder.matches(newPassword, user.getPassword());
	}

	public void verifyUsernameAndRecoveryToken(String username, String recoveryToken) {
		String storedUsername = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + recoveryToken);
		if (storedUsername == null) {
			throw new PasswordRecoverException("Token không tồn tại hoặc đã hết hạn.", HttpStatus.BAD_REQUEST);
		}

		if (!storedUsername.equals(username)) {
			throw new PasswordRecoverException("Token không khớp với tài khoản này.", HttpStatus.BAD_REQUEST);
		}

	}

	public boolean verifyUsernameAndRecoveryTokenForResponse(String username, String recoveryToken) {
		String storedUsername = redisTemplate.opsForValue().get(REDIS_TOKEN_PREFIX + recoveryToken);
		if (storedUsername == null) {
			return false;
		}

		if (!storedUsername.equals(username)) {
			return false;
		}

		return true;
	}

	public boolean checkConfirmNewPassword(String newPassword, String confirmNewPassword) {
		return newPassword.equals(confirmNewPassword);
	}

	private static String generateRandomToken() {
		return UUID.randomUUID().toString();
	}

	private static String getBrowser(String userAgent) {
		if (userAgent.contains("Chrome")) {
			return "Google Chrome";
		} else if (userAgent.contains("Firefox")) {
			return "Mozilla Firefox";
		} else if (userAgent.contains("Safari")) {
			return "Safari";
		} else if (userAgent.contains("Edge")) {
			return "Microsoft Edge";
		} else {
			return "Unknown Browser";
		}
	}

	private static String getOS(String userAgent) {
		if (userAgent.contains("Windows NT")) {
			return "Windows";
		} else if (userAgent.contains("Mac OS X")) {
			return "Mac OS";
		} else if (userAgent.contains("Linux")) {
			return "Linux";
		} else if (userAgent.contains("Android")) {
			return "Android";
		} else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
			return "iOS";
		} else {
			return "Unknown OS";
		}
	}

	private static String getDevice(String userAgent) {
		if (userAgent.contains("Mobi")) {
			return "Mobile Device";
		} else if (userAgent.contains("Tablet")) {
			return "Tablet";
		} else {
			return "Desktop";
		}
	}
}