package com.shopaccgame.controllers;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.shopaccgame.dtos.error.ErrorResponseDTO;
import com.shopaccgame.exceptions.common.ForbiddenException;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotAvailableException;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotFoundException;
import com.shopaccgame.exceptions.promotion.giftcode.GiftcodeNotAvailableException;
import com.shopaccgame.exceptions.promotion.giftcode.GiftcodeNotFoundException;
import com.shopaccgame.exceptions.promotion.voucher.VoucherNotAvailableException;
import com.shopaccgame.exceptions.promotion.voucher.VoucherNotFoundException;
import com.shopaccgame.exceptions.transaction.deposit.CardDepositException;
import com.shopaccgame.exceptions.transaction.gameaccount.GameAccountTransactionException;
import com.shopaccgame.exceptions.user.UpdateUserStatusException;
import com.shopaccgame.exceptions.user.UserNotFoundException;
import com.shopaccgame.exceptions.user.authentication.LoginException;
import com.shopaccgame.exceptions.user.authentication.LogoutException;
import com.shopaccgame.exceptions.user.authentication.PasswordRecoverException;
import com.shopaccgame.exceptions.user.authentication.RefreshTokenException;
import com.shopaccgame.exceptions.user.authentication.RegisterException;
import com.shopaccgame.exceptions.user.authentication.TokenAuthenticationException;
import com.shopaccgame.services.user.authentication.LoginAttemptService;

import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestControllerAdvice
public class ExceptionHandlerController {

	private final LoginAttemptService loginAttemptService;

	public ExceptionHandlerController(LoginAttemptService loginAttemptService) {
		this.loginAttemptService = loginAttemptService;
	}

	@ExceptionHandler(RegisterException.class)
	public ResponseEntity<ErrorResponseDTO> handleRegisterException(RegisterException ex) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("REGISTER_FAILED", ex.getMessage());
		return new ResponseEntity<>(errorResponseDTO, ex.getStatus());
	}

	@ExceptionHandler(LoginException.class)
	public ResponseEntity<ErrorResponseDTO> handleLoginException(LoginException ex) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("LOGIN_FAILED", ex.getMessage());
		return new ResponseEntity<>(errorResponseDTO, ex.getStatus());
	}

	@ExceptionHandler(LogoutException.class)
	public ResponseEntity<ErrorResponseDTO> handleLogoutException(LogoutException ex) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("LOGOUT_FAILED", ex.getMessage());
		return new ResponseEntity<>(errorResponseDTO, ex.getStatus());
	}

	@ExceptionHandler(RefreshTokenException.class)
	public ResponseEntity<ErrorResponseDTO> handleRefreshTokenException(RefreshTokenException ex) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO("REFRESH_TOKEN_FAILED", ex.getMessage());
		return new ResponseEntity<>(errorResponseDTO, ex.getStatus());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException ex,
			HttpServletRequest request) {
		String username = (String) request.getAttribute("loginUsername");
		if (username == null || username.trim().isEmpty()) {
			username = "unknown";
		}

		loginAttemptService.loginFailed(username);
		Optional<ErrorResponseDTO> lockStatus = loginAttemptService.getAccountLockStatus(username);
		if (lockStatus.isPresent()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(lockStatus.get());
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDTO("LOGIN_FAILED", "Tài khoản hoặc mật khẩu không đúng!"));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponseDTO> handleForbiddenException(ForbiddenException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("FORBIDDEN", ex.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("ACCESS_DENIED", ex.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(TokenAuthenticationException.class)
	public ResponseEntity<ErrorResponseDTO> handleTokenAuthenticationException(TokenAuthenticationException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("AUTHENTICATION_FAILED", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("USER_NOT_FOUND", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(GameAccountTransactionException.class)
	public ResponseEntity<ErrorResponseDTO> handleAccountTransactionException(GameAccountTransactionException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("INSUFFICIENT_BALANCE", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(GameAccountNotAvailableException.class)
	public ResponseEntity<ErrorResponseDTO> handleAccountNotAvailableException(GameAccountNotAvailableException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("ACCOUNT_NOT_AVAILABLE", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(GiftcodeNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleGiftcodeNotFoundException(GiftcodeNotFoundException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("GIFTCODE_NOT_FOUND", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(GiftcodeNotAvailableException.class)
	public ResponseEntity<ErrorResponseDTO> handleGiftcodeNotAvailableException(GiftcodeNotAvailableException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("GIFTCODE_NOT_AVAILABLE", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(VoucherNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleVoucherNotFoundException(VoucherNotFoundException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("VOUCHER_NOT_FOUND", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(VoucherNotAvailableException.class)
	public ResponseEntity<ErrorResponseDTO> handleVoucherNotAvailableException(VoucherNotAvailableException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("VOUCHER_NOT_AVAILABLE", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(CardDepositException.class)
	public ResponseEntity<ErrorResponseDTO> handleCardDepositException(CardDepositException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("REQUEST_CARD_DEPOSIT_FAILED", ex.getMessage()),
				ex.getStatus());
	}

	@ExceptionHandler(PasswordRecoverException.class)
	public ResponseEntity<ErrorResponseDTO> handlePasswordRecoverException(PasswordRecoverException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("PASSWORD_RECOVER_FAILED", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(UpdateUserStatusException.class)
	public ResponseEntity<ErrorResponseDTO> handleUpdateUserStatusException(UpdateUserStatusException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("UPDATE_USER_STATUS_FAILED", ex.getMessage()), ex.getStatus());
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponseDTO> handleNoSuchElementException(NoSuchElementException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("RESOURCE_NOT_FOUND", ex.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MessagingException.class)
	public ResponseEntity<ErrorResponseDTO> handleMessagingException(MessagingException ex) {
		return new ResponseEntity<>(
				new ErrorResponseDTO("EMAIL_SEND_FAILED", "Không thể gửi email: " + ex.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MailException.class)
	public ResponseEntity<ErrorResponseDTO> handleMailException(MailException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("EMAIL_SEND_FAILED", "Lỗi khi gửi email: " + ex.getMessage()),
				HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(GameAccountNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleGameAccountNotFoundException(GameAccountNotFoundException ex) {
		return new ResponseEntity<>(
				new ErrorResponseDTO("GAMEACCOUNT_NOT_FOUND", "Tài khoản Game không tồn tại: " + ex.getMessage()),
				ex.getStatus());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("INVALID_ACCOUNT_TYPE", "Đã xảy ra lỗi: " + ex.getMessage()),
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String errorMessages = ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return new ResponseEntity<>(new ErrorResponseDTO("VALIDATION_ERROR", errorMessages), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
		return new ResponseEntity<>(new ErrorResponseDTO("SERVER_ERROR", "Lỗi hệ thống: " + ex.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}