package com.shopaccgame.controllers;

import com.shopaccgame.dtos.error.ErrorResponseDTO;
import com.shopaccgame.dtos.promotion.giftcode.GiftcodeRequestDTO;
import com.shopaccgame.dtos.promotion.giftcode.GiftcodeResponseDTO;
import com.shopaccgame.dtos.promotion.voucher.VoucherRequestDTO;
import com.shopaccgame.dtos.promotion.voucher.VoucherResponseDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositRequestDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositResponseDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositTransactionDTO;
import com.shopaccgame.dtos.transaction.deposit.DepositPaymentInfoDTO;
import com.shopaccgame.dtos.transaction.deposit.VNPayRequestDTO;
import com.shopaccgame.dtos.transaction.deposit.VNPayResponseDTO;
import com.shopaccgame.dtos.transaction.deposit.VNPayResultDTO;
import com.shopaccgame.dtos.transaction.deposit.VNPayTransactionDTO;
import com.shopaccgame.dtos.transaction.gameaccount.GameAccountTransactionDTO;
import com.shopaccgame.dtos.transaction.purchase.PurchaseRequestDTO;
import com.shopaccgame.dtos.transaction.purchase.PurchaseResponseDTO;
import com.shopaccgame.dtos.user.UserDTO;
import com.shopaccgame.dtos.userauthentication.ChangePasswordDTO;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.exceptions.common.ForbiddenException;
import com.shopaccgame.models.promotion.voucher.Voucher;
import com.shopaccgame.models.transaction.deposit.CardDepositOrder;
import com.shopaccgame.models.transaction.deposit.VNPayTransaction;
import com.shopaccgame.models.transaction.gameaccount.GameAccountTransaction;
import com.shopaccgame.models.user.User;
import com.shopaccgame.services.promotion.giftcode.GiftcodeService;
import com.shopaccgame.services.promotion.voucher.VoucherService;
import com.shopaccgame.services.user.authentication.ChangePasswordService;
import com.shopaccgame.services.user.transaction.deposit.CardDepositService;
import com.shopaccgame.services.user.transaction.deposit.DepositPaymentInfoService;
import com.shopaccgame.services.user.transaction.deposit.VNPayService;
import com.shopaccgame.services.user.transaction.deposit.VNPayTransactionService;
import com.shopaccgame.services.user.transaction.gameaccount.GameAccountTransactionService;
import com.shopaccgame.utils.AuthenticationUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "APIs related to User")

public class UserController {
	private final GiftcodeService giftcodeService;
	private final GameAccountTransactionService gameAccountTransactionService;
	private final VoucherService voucherService;
	private final ChangePasswordService changePasswordService;
	private final VNPayService vnPayService;
	private final VNPayTransactionService vnPayTransactionService;
	private final CardDepositService cardDepositService;
	private final DepositPaymentInfoService depositPaymentInfoService;

	public UserController(GiftcodeService giftcodeService, GameAccountTransactionService gameAccountTransactionService,
			VoucherService voucherService, ChangePasswordService changePasswordService, VNPayService vnPayService,
			VNPayTransactionService vnPayTransactionService, CardDepositService cardDepositService,
			DepositPaymentInfoService depositPaymentInfoService) {
		this.giftcodeService = giftcodeService;
		this.gameAccountTransactionService = gameAccountTransactionService;
		this.voucherService = voucherService;
		this.changePasswordService = changePasswordService;
		this.vnPayService = vnPayService;
		this.vnPayTransactionService = vnPayTransactionService;
		this.cardDepositService = cardDepositService;
		this.depositPaymentInfoService = depositPaymentInfoService;
	}

	@Operation(summary = "Get information related to the user", description = "Get relevant information of a user account on the system")
	@GetMapping("/me")
	public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}
		UserDTO usersDTO = EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
		return ResponseEntity.ok(usersDTO);
	}

	@Operation(summary = "Update the new password of the user account", description = "Update the new password of the user account on the system")
	@PatchMapping("/password")
	public ResponseEntity<Map<String, Object>> changePassword(Authentication authentication,
			@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
		Map<String, Object> response = new HashMap<>();

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user == null || authentication == null || !authentication.isAuthenticated()
				|| authentication.getPrincipal() instanceof String) {
			response.put("message", "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
			response.put("errorCode", "UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}

		String username = authentication.getName();

		if (changePasswordDTO.getNewPassword().equals(username)) {
			response.put("message", "Mật khẩu không được trùng với tên tài khoản.");
			response.put("errorCode", "INVALID_PASSWORD");
			return ResponseEntity.badRequest().body(response);
		}

		if (!changePasswordService.CheckCurrentPassword(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
			response.put("message", "Mật khẩu hiện tại không chính xác.");
			response.put("errorCode", "WRONG_CURRENT_PASSWORD");
			return ResponseEntity.badRequest().body(response);
		}

		if (changePasswordService.CheckCurrentPasswordAndNewPassword(changePasswordDTO.getNewPassword(),
				user.getPassword())) {
			response.put("message", "Mật khẩu mới không được trùng với mật khẩu hiện tại.");
			response.put("errorCode", "SAME_AS_CURRENT_PASSWORD");
			return ResponseEntity.badRequest().body(response);
		}

		if (!changePasswordService.CheckConfirmNewPassword(changePasswordDTO.getNewPassword(),
				changePasswordDTO.getConfirmNewPassword())) {
			response.put("message", "Xác nhận mật khẩu mới không khớp.");
			response.put("errorCode", "PASSWORD_MISMATCH");
			return ResponseEntity.badRequest().body(response);
		}

		changePasswordService.UpdateNewPassword(username, changePasswordDTO.getNewPassword());

		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get detailed information related to deposit via various methods", description = "Get detailed information related to deposit via various methods on the system")
	@GetMapping("/deposit-payment-info")
	public ResponseEntity<DepositPaymentInfoDTO> getCardTradeCost(Authentication authentication,
			HttpServletRequest request) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}
		DepositPaymentInfoDTO depositPaymentInfoDTO = EntityConvertToDTOUtil
				.convertToDTO(depositPaymentInfoService.getDepositPaymentInfo(), DepositPaymentInfoDTO.class);
		return ResponseEntity.ok(depositPaymentInfoDTO);
	}

	@Operation(summary = "Get the history containing all deposit transactions via VNPay of a user", description = "Get the history containing all deposit transactions via VNPay of a user on the system")
	@GetMapping("/transactions/vnpay")
	public ResponseEntity<Page<VNPayTransactionDTO>> VNPayDepositHistory(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "timeOfDepositing,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}
		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<VNPayTransaction> transactions = vnPayTransactionService.getVNPayDepositOrders(pageable, user);

		List<VNPayTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, VNPayTransactionDTO.class))
				.collect(Collectors.toList());

		Page<VNPayTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Returns deposit results via VNPay method", description = "Returns deposit results via VNPay method")
	@GetMapping("/transactions/vnpay/return")
	public ResponseEntity<?> returnVNPayOrder(Authentication authentication, HttpServletRequest request) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);

		int paymentStatus = vnPayService.orderReturn(request);

		String orderInfo = request.getParameter("vnp_OrderInfo");
		String paymentTime = request.getParameter("vnp_PayDate");
		String transactionId = request.getParameter("vnp_TransactionNo");
		String totalPrice = request.getParameter("vnp_Amount");
		String txnRef = request.getParameter("vnp_TxnRef");

		if (paymentStatus == -1) {
			return ResponseEntity.status(400)
					.body(new ErrorResponseDTO("VNPAY_SIGNATURE_ERROR", "Chữ ký không hợp lệ"));
		}

		VNPayResultDTO result = new VNPayResultDTO(orderInfo != null ? orderInfo : "N/A",
				totalPrice != null ? totalPrice : "0", paymentTime != null ? paymentTime : "N/A",
				transactionId != null ? transactionId : "N/A", paymentStatus == 1);

		if (paymentStatus == 1) {
			long amount = Long.parseLong(totalPrice) / 100;
			vnPayTransactionService.processSuccessfulPayment(user, amount, transactionId, txnRef);
		}

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Create a new deposit request via VNPay method", description = "Create a new deposit request via VNPay method")
	@PostMapping("/transactions/vnpay")
	public ResponseEntity<VNPayResponseDTO> submitVNPayOrder(Authentication authentication,
			@Valid @RequestBody VNPayRequestDTO requestDTO, HttpServletRequest request) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}
		String orderInfo = "Nap tien " + requestDTO.getAmount() + " VND cho user " + user.getId();
		String returnUrl = "https://localhost:3000/pages/client/member/recharge.html";
		String vnpayUrl = vnPayService.createOrder(requestDTO.getAmount(), orderInfo, returnUrl);
		return ResponseEntity.ok(new VNPayResponseDTO(vnpayUrl));
	}

	@Operation(summary = "Get the history containing all deposit cards of a user", description = "Get the history containing all deposit cards of a user on the system")
	@GetMapping("/transactions/card-deposits")
	public ResponseEntity<Page<CardDepositTransactionDTO>> CardDepositHistory(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "timeOfDepositing,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<CardDepositOrder> transactions = cardDepositService.getCardDepositOrderByUser(pageable, user);

		List<CardDepositTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, CardDepositTransactionDTO.class))
				.collect(Collectors.toList());

		Page<CardDepositTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Create a new deposit request via deposit card", description = "Create a new deposit request via deposit card")
	@PostMapping("/transactions/card-deposits")
	public ResponseEntity<CardDepositResponseDTO> depositWithCard(Authentication authentication,
			@Valid @RequestBody CardDepositRequestDTO cardDepositRequestDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}

		CardDepositResponseDTO cardDepositResponseDTO = cardDepositService.submitDepositCard(cardDepositRequestDTO,
				user);
		return ResponseEntity.ok(cardDepositResponseDTO);
	}

	@Operation(summary = "Get the history containing all purchases of a user's game account", description = "Get the history containing all purchases of a user's game account")
	@GetMapping("/transactions/gameaccounts")
	public ResponseEntity<Page<GameAccountTransactionDTO>> getPurchaseHistory(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "transactionDate,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<GameAccountTransaction> transactions = gameAccountTransactionService.getPurchaseHistory(pageable, user);

		List<GameAccountTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, GameAccountTransactionDTO.class))
				.collect(Collectors.toList());

		Page<GameAccountTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Buy a Game account", description = "Buy a Game account on the system")
	@PostMapping("/transactions/gameaccounts")
	public ResponseEntity<PurchaseResponseDTO> purchaseAccount(Authentication authentication,
			@Valid @RequestBody PurchaseRequestDTO purchaseRequestDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);

		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}

		PurchaseResponseDTO purchaseResponseDTO = gameAccountTransactionService.purchaseAccount(user,
				purchaseRequestDTO.getAccountId(), purchaseRequestDTO.getGameAccountType(),
				purchaseRequestDTO.getVoucher());

		return ResponseEntity.status(HttpStatus.CREATED).body(purchaseResponseDTO);
	}

	@Operation(summary = "Apply Voucher to purchase Game account to receive discount", description = "Apply Voucher to purchase Game account to receive discount")
	@PostMapping("/vouchers/apply")
	public ResponseEntity<VoucherResponseDTO> applyVoucher(@Valid @RequestBody VoucherRequestDTO voucherRequestDTO) {
		Voucher voucher = voucherService.applyVoucher(voucherRequestDTO.getCode());
		VoucherResponseDTO voucherResponseDTO = EntityConvertToDTOUtil.convertToDTO(voucher, VoucherResponseDTO.class);
		voucherResponseDTO.setMessage("Áp dụng voucher thành công! Giảm " + voucher.getValue() + " VNĐ");
		return ResponseEntity.ok(voucherResponseDTO);
	}

	@Operation(summary = "Activate Giftcode to receive gifts", description = "Activate Giftcode to receive gifts: Add to current user's balance")
	@PostMapping("/giftcodes/activate")
	public ResponseEntity<GiftcodeResponseDTO> receiveGiftcode(Authentication authentication,
			@Valid @RequestBody GiftcodeRequestDTO giftcodeRequestDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, bạn không thể sử dụng tính năng này!",
					HttpStatus.FORBIDDEN);
		}
		GiftcodeResponseDTO responseDTO = giftcodeService.receiveGiftcode(user.getId(), giftcodeRequestDTO.getCode());
		return ResponseEntity.ok(responseDTO);
	}
}