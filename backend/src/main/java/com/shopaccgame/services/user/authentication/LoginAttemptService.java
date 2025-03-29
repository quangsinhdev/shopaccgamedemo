package com.shopaccgame.services.user.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.shopaccgame.dtos.error.ErrorResponseDTO;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

	private RedisTemplate<String, Object> redisTemplate;

	@Value("${login.Failed.Count.Attempt}")
	private int loginFailedCountAttempt;

	@Value("${lock.Time}")
	private int lockTimeFollowMinutes;

	private static final String ATTEMPTS_KEY_PREFIX = "login:attempts:";
	private static final String LOCK_KEY_PREFIX = "login:lock:";

	public LoginAttemptService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public int getLockTimeFollowMinutes() {
		return lockTimeFollowMinutes;
	}

	public boolean checkAccountLocked(String username) {
		String lockKey = LOCK_KEY_PREFIX + username;
		Boolean isLocked = (Boolean) redisTemplate.opsForValue().get(lockKey);
		return Boolean.TRUE.equals(isLocked);
	}

	public void loginFailed(String username) {
		try {
			String attemptsKey = ATTEMPTS_KEY_PREFIX + username;
			String lockKey = LOCK_KEY_PREFIX + username;

			Long attempts = redisTemplate.opsForValue().increment(attemptsKey, 1L);
			if (attempts == null) {
				return;
			}

			redisTemplate.expire(attemptsKey, lockTimeFollowMinutes, TimeUnit.MINUTES);

			if (attempts >= loginFailedCountAttempt) {
				redisTemplate.opsForValue().setIfAbsent(lockKey, true, lockTimeFollowMinutes, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to update login attempts in Redis", e);
		}
	}

	public void resetLoginAttempts(String username) {
		String attemptsKey = ATTEMPTS_KEY_PREFIX + username;
		String lockKey = LOCK_KEY_PREFIX + username;
		redisTemplate.delete(attemptsKey);
		redisTemplate.delete(lockKey);
	}

	public int getLoginAttempts(String username) {
		String attemptsKey = ATTEMPTS_KEY_PREFIX + username;
		Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
		return attempts != null ? attempts : 0;
	}

	public long getLockTime(String username) {
		String lockKey = LOCK_KEY_PREFIX + username;
		Long remainingTime = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
		return remainingTime != null && remainingTime > 0 ? remainingTime : 0L;
	}

	public Optional<ErrorResponseDTO> getAccountLockStatus(String username) {
		if (checkAccountLocked(username)) {
			long remainingTimeInSeconds = getLockTime(username);
			long remainingTimeInMinutes = (remainingTimeInSeconds < 60) ? 1
					: (long) Math.ceil(remainingTimeInSeconds / 60.0);
			return Optional.of(new ErrorResponseDTO("ACCOUNT_LOCKED",
					"Tài khoản bị khóa do đăng nhập sai quá nhiều. Thử lại sau: " + remainingTimeInMinutes + " phút"));
		}
		return Optional.empty();
	}

}