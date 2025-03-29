package com.shopaccgame.controllers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopaccgame.dtos.gameaccount.FifaAccountDTO;
import com.shopaccgame.dtos.gameaccount.LOLAccountDTO;
import com.shopaccgame.dtos.gameaccount.LQMAccountDTO;
import com.shopaccgame.dtos.gameaccount.PostGameAccountDTO;
import com.shopaccgame.dtos.promotion.giftcode.GiftcodeDTO;
import com.shopaccgame.dtos.promotion.voucher.VoucherDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositApprovalDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositTransactionDTO;
import com.shopaccgame.dtos.transaction.deposit.DepositPaymentInfoDTO;
import com.shopaccgame.dtos.transaction.deposit.VNPayTransactionDTO;
import com.shopaccgame.dtos.transaction.gameaccount.GameAccountTransactionDTO;
import com.shopaccgame.dtos.user.UserDTO;
import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.enums.gameaccount.GameAccountType;
import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.exceptions.common.ForbiddenException;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotFoundException;
import com.shopaccgame.models.gameaccount.fifa.FifaAccount;
import com.shopaccgame.models.gameaccount.lol.LOLAccount;
import com.shopaccgame.models.gameaccount.lqm.LQMAccount;
import com.shopaccgame.models.promotion.giftcode.Giftcode;
import com.shopaccgame.models.promotion.voucher.Voucher;
import com.shopaccgame.models.transaction.deposit.CardDepositOrder;
import com.shopaccgame.models.transaction.deposit.VNPayTransaction;
import com.shopaccgame.models.transaction.gameaccount.GameAccountTransaction;
import com.shopaccgame.models.user.User;
import com.shopaccgame.services.gameaccount.FifaService;
import com.shopaccgame.services.gameaccount.LOLService;
import com.shopaccgame.services.gameaccount.LQMService;
import com.shopaccgame.services.promotion.giftcode.GiftcodeService;
import com.shopaccgame.services.promotion.voucher.VoucherService;
import com.shopaccgame.services.user.UserService;
import com.shopaccgame.services.user.transaction.deposit.CardDepositService;
import com.shopaccgame.services.user.transaction.deposit.DepositPaymentInfoService;
import com.shopaccgame.services.user.transaction.deposit.VNPayTransactionService;
import com.shopaccgame.services.user.transaction.gameaccount.GameAccountTransactionService;
import com.shopaccgame.utils.AuthenticationUtil;
import com.shopaccgame.utils.ConvertToAccountCategoryDTOUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administrator API", description = "APIs for managing administrators")
public class AdministratorController {

	private final FifaService fifaService;
	private final LOLService lolService;
	private final LQMService lqmService;
	private final GiftcodeService giftcodeService;
	private final VoucherService voucherService;
	private final UserService userService;
	private final CardDepositService cardDepositService;
	private final VNPayTransactionService vnPayTransactionService;
	private final GameAccountTransactionService gameAccountTransactionService;
	private final DepositPaymentInfoService depositPaymentInfoService;

	public AdministratorController(FifaService fifaService, LOLService lolService, LQMService lqmService,
			GiftcodeService giftcodeService, VoucherService voucherService, UserService userService,
			CardDepositService cardDepositService, VNPayTransactionService vnPayTransactionService,
			GameAccountTransactionService gameAccountTransactionService,
			DepositPaymentInfoService depositPaymentInfoService) {
		this.fifaService = fifaService;
		this.lolService = lolService;
		this.lqmService = lqmService;
		this.giftcodeService = giftcodeService;
		this.voucherService = voucherService;
		this.userService = userService;
		this.cardDepositService = cardDepositService;
		this.vnPayTransactionService = vnPayTransactionService;
		this.gameAccountTransactionService = gameAccountTransactionService;
		this.depositPaymentInfoService = depositPaymentInfoService;
	}

	@Operation(summary = "Get all users", description = "Returns the entire list of users")
	@GetMapping("/users")
	public ResponseEntity<Page<UserDTO>> getAllUsers(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String userStatus, @RequestParam(required = false) String role) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		Pageable pageable = PageRequest.of(page, size);

		UserStatus status = null;
		UserRole userRole = null;

		if (userStatus != null && !userStatus.isEmpty()) {
			status = UserStatus.valueOf(userStatus.toUpperCase());
		}
		if (role != null && !role.isEmpty()) {
			userRole = UserRole.valueOf(role.toUpperCase());
		}

		Page<User> userPage = userService.getUsersFollowFilter(pageable, status, userRole);

		Page<UserDTO> userDTOPage = userPage.map(u -> {
			return EntityConvertToDTOUtil.convertToDTO(u, UserDTO.class);
		});
		return ResponseEntity.ok(userDTOPage);
	}

	@Operation(summary = "Get user by ID User", description = "Returns details of a user based on user ID")
	@GetMapping("/users/{id}")
	public ResponseEntity<UserDTO> getUserById(Authentication authentication, @PathVariable Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		UserDTO userDTO = userService.getUserById(id);
		return ResponseEntity.ok(userDTO);
	}

	@Operation(summary = "Update user by ID User", description = "Update a user's details based on the user ID")
	@PutMapping("/users/{id}")
	public ResponseEntity<UserDTO> updateUserById(Authentication authentication, @PathVariable Long id,
			@Valid @RequestBody UserDTO userDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		if (userDTO.getId() != null && !id.equals(userDTO.getId())) {
			throw new IllegalArgumentException("Đã xảy ra lỗi khi cập nhật người dùng.");
		}
		UserDTO updatedUserDTO = userService.updateUser(id, userDTO);
		return ResponseEntity.ok(updatedUserDTO);
	}

	@Operation(summary = "Delete user by ID User", description = "Remove a user from the system based on user ID")
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUserById(Authentication authentication, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Create a new Game Account for sale", description = "Create a new Game for sale account on the system")
	@PostMapping("/gameaccounts")
	public ResponseEntity<?> postNewGameAccount(Authentication authentication,
			@Valid @RequestBody PostGameAccountDTO postGameAccountDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		switch (postGameAccountDTO.getGameAccountType()) {
		case FIFA:
			FifaAccountDTO fifaAccountDTO = (FifaAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			FifaAccountDTO savedFifaAccountDTO = fifaService.createFifaAccount(fifaAccountDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedFifaAccountDTO);
		case LOL:
			LOLAccountDTO lolAccountDTO = (LOLAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			LOLAccountDTO savedLOLAccountDTO = lolService.createLOLAccount(lolAccountDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedLOLAccountDTO);
		case LQ:
			LQMAccountDTO lqmAccountDTO = (LQMAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			LQMAccountDTO savedLQMAccountDTO = lqmService.createLQMAccount(lqmAccountDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(savedLQMAccountDTO);
		default:
			throw new IllegalArgumentException(
					"GameAccountType không được hỗ trợ: " + postGameAccountDTO.getGameAccountType());
		}
	}

	@Operation(summary = "Get Game accounts of a specific Game category", description = "Get the entire list of Game accounts based on Game category")
	@GetMapping("/gameaccounts/{gameaccounttype}")
	public ResponseEntity<?> getGameAccountsByType(Authentication authentication,
			@PathVariable("gameaccounttype") String gameAccountType,
			@RequestParam(value = "status", required = false) String status, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), "price"));

		GameAccountType accountType;
		GameAccountStatus gameAccountStatus;
		accountType = GameAccountType.valueOf(gameAccountType.toUpperCase());
		gameAccountStatus = status != null ? GameAccountStatus.valueOf(status.toUpperCase()) : null;

		switch (accountType) {
		case FIFA:
			Page<FifaAccount> fifaAccounts = fifaService.getFifaAccountsByStatus(pageable, gameAccountStatus);
			Page<FifaAccountDTO> fifaAccountDTOs = fifaAccounts
					.map(account -> EntityConvertToDTOUtil.convertToDTO(account, FifaAccountDTO.class));
			return ResponseEntity.ok(fifaAccountDTOs);
		case LOL:
			Page<LOLAccount> lolAccounts = lolService.getLOLAccountsByStatus(pageable, gameAccountStatus);
			Page<LOLAccountDTO> lolAccountDTOs = lolAccounts
					.map(account -> EntityConvertToDTOUtil.convertToDTO(account, LOLAccountDTO.class));
			return ResponseEntity.ok(lolAccountDTOs);
		case LQ:
			Page<LQMAccount> lqmAccounts = lqmService.getLQMAccountsByStatus(pageable, gameAccountStatus);
			Page<LQMAccountDTO> lqmAccountDTOs = lqmAccounts
					.map(account -> EntityConvertToDTOUtil.convertToDTO(account, LQMAccountDTO.class));
			return ResponseEntity.ok(lqmAccountDTOs);
		default:
			throw new GameAccountNotFoundException("GameAccountType không được hỗ trợ. ", HttpStatus.BAD_REQUEST);
		}
	}

	@Operation(summary = "Get details of a Game account based on Game category and Game account ID", description = "Get all detailed information of a game account based on the game account ID")
	@GetMapping("/gameaccounts/{gameaccounttype}/{id}")
	public ResponseEntity<?> getGameAccountById(Authentication authentication,
			@PathVariable("gameaccounttype") String gameAccountType, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		GameAccountType accountType = GameAccountType.valueOf(gameAccountType.toUpperCase());
		switch (accountType) {
		case FIFA:
			FifaAccount fifaaccount = fifaService.getFifaAccountById(id);
			FifaAccountDTO fifaAccountDTO = EntityConvertToDTOUtil.convertToDTO(fifaaccount, FifaAccountDTO.class);
			return ResponseEntity.ok(fifaAccountDTO);
		case LOL:
			LOLAccount lolAccount = lolService.getLOLAccountById(id);
			LOLAccountDTO lolAccountDTO = EntityConvertToDTOUtil.convertToDTO(lolAccount, LOLAccountDTO.class);
			return ResponseEntity.ok(lolAccountDTO);
		case LQ:
			LQMAccount lqmAccount = lqmService.getLQMAccountById(id);
			LQMAccountDTO lqmAccountDTO = EntityConvertToDTOUtil.convertToDTO(lqmAccount, LQMAccountDTO.class);
			return ResponseEntity.ok(lqmAccountDTO);
		default:
			throw new IllegalArgumentException("GameAccountType không được hỗ trợ: " + gameAccountType);
		}
	}

	@Operation(summary = "Update details of a Game account based on Game catogory and Game Account ID", description = "Update detailed information of a Game account based on the game account ID")
	@PutMapping("/gameaccounts/{gameaccounttype}/{id}")
	public ResponseEntity<?> updateGameAccountById(Authentication authentication,
			@PathVariable("gameaccounttype") String gameAccountType, @PathVariable("id") Long id,
			@Valid @RequestBody PostGameAccountDTO postGameAccountDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		GameAccountType accountType = GameAccountType.valueOf(gameAccountType.toUpperCase());
		if (accountType != postGameAccountDTO.getGameAccountType()) {
			return ResponseEntity.badRequest().body("Đã xảy ra lỗi khi cập nhật tài khoản Game.");
		}
		switch (accountType) {
		case FIFA:
			FifaAccountDTO fifaAccountDTO = (FifaAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			fifaAccountDTO.setId(id);
			FifaAccountDTO updatedFifaAccountDTO = fifaService.updateFifaAccount(fifaAccountDTO);
			return ResponseEntity.ok(updatedFifaAccountDTO);
		case LOL:
			LOLAccountDTO lolAccountDTO = (LOLAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			lolAccountDTO.setId(id);
			LOLAccountDTO updatedLOLAccountDTO = lolService.updateLOLAccount(lolAccountDTO);
			return ResponseEntity.ok(updatedLOLAccountDTO);
		case LQ:
			LQMAccountDTO lqmAccountDTO = (LQMAccountDTO) ConvertToAccountCategoryDTOUtil
					.convertToSpecificAccountDTO(postGameAccountDTO);
			lqmAccountDTO.setId(id);
			LQMAccountDTO updatedLQMAccountDTO = lqmService.updateLQMAccount(lqmAccountDTO);
			return ResponseEntity.ok(updatedLQMAccountDTO);
		default:
			throw new IllegalArgumentException("GameAccountType không được hỗ trợ: " + gameAccountType);
		}
	}

	@Operation(summary = "Delete a Game account based on Game category and Game account ID", description = "Delete a game account based on the game account ID")
	@DeleteMapping("/gameaccounts/{gameaccounttype}/{id}")
	public ResponseEntity<Void> deleteGameAccountById(Authentication authentication,
			@PathVariable("gameaccounttype") String gameAccountType, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		GameAccountType accountType = GameAccountType.valueOf(gameAccountType.toUpperCase());
		switch (accountType) {
		case FIFA:
			fifaService.deleteFifaAccountById(id);
			break;
		case LOL:
			lolService.deleteLOLAccountById(id);
			break;
		case LQ:
			lqmService.deleteLQMAccountById(id);
			break;
		default:
			throw new IllegalArgumentException("GameAccountType không được hỗ trợ: " + gameAccountType);
		}
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Create a new giftcode", description = "Create a new Giftcode on the system")
	@PostMapping("/giftcodes")
	public ResponseEntity<GiftcodeDTO> postNewGiftcode(Authentication authentication,
			@Valid @RequestBody GiftcodeDTO giftcodeDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		GiftcodeDTO savedGiftcodeDTO = giftcodeService.createGiftcode(giftcodeDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedGiftcodeDTO);
	}

	@Operation(summary = "Get all giftcode", description = "Get all Giftcodes on the system (including unused and used)")
	@GetMapping("/giftcodes")
	public ResponseEntity<Page<GiftcodeDTO>> getAllGiftcodes(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id,asc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<Giftcode> giftcodes = giftcodeService.getAllGiftcodes(pageable);

		List<GiftcodeDTO> giftcodeDTOs = giftcodes.getContent().stream()
				.map(giftcode -> EntityConvertToDTOUtil.convertToDTO(giftcode, GiftcodeDTO.class))
				.collect(Collectors.toList());

		Page<GiftcodeDTO> giftcodeDTOPage = new PageImpl<>(giftcodeDTOs, pageable, giftcodes.getTotalElements());

		return ResponseEntity.ok(giftcodeDTOPage);
	}

	@Operation(summary = "Get Giftcode details based on Giftcode ID", description = "Get detailed information of a Giftcode based on the giftcode ID")
	@GetMapping("/giftcodes/{id}")
	public ResponseEntity<GiftcodeDTO> getGiftcodeById(Authentication authentication, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		GiftcodeDTO giftcodeDTO = giftcodeService.getGiftcodeById(id);
		return ResponseEntity.ok(giftcodeDTO);
	}

	@Operation(summary = "Update Giftcode details based on Giftcode ID", description = "Update the details of a Giftcode based on the giftcode ID")
	@PutMapping("/giftcodes/{id}")
	public ResponseEntity<GiftcodeDTO> updateGiftcodeById(Authentication authentication, @PathVariable("id") Long id,
			@Valid @RequestBody GiftcodeDTO giftcodeDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		if (giftcodeDTO.getId() != null && !id.equals(giftcodeDTO.getId())) {
			throw new IllegalArgumentException("Đã xảy ra lỗi khi cập nhật Giftcode.");
		}
		giftcodeDTO.setId(id);
		GiftcodeDTO giftcodeSavedDTO = giftcodeService.updateGiftcode(id, giftcodeDTO);
		return ResponseEntity.ok(giftcodeSavedDTO);
	}

	@Operation(summary = "Delete a Giftcode based on Giftcode ID", description = "Delete a Giftcode from the system based on the giftcode ID")
	@DeleteMapping("/giftcodes/{id}")
	public ResponseEntity<Void> deleteGiftcodeById(Authentication authentication, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		giftcodeService.deleteGiftcodeById(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Create a new Voucher", description = "Create a new Voucher on the system")
	@PostMapping("/vouchers")
	public ResponseEntity<VoucherDTO> postNewVoucher(Authentication authentication,
			@Valid @RequestBody VoucherDTO voucherDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		VoucherDTO savedVoucherDTO = voucherService.createVoucher(voucherDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedVoucherDTO);
	}

	@Operation(summary = "Get all Voucher", description = "Get all vouchers on the system (including unused and used)")
	@GetMapping("/vouchers")
	public ResponseEntity<Page<VoucherDTO>> getAllVouchers(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id,asc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<Voucher> vouchers = voucherService.getAllVouchers(pageable);

		List<VoucherDTO> voucherDTOs = vouchers.getContent().stream()
				.map(voucher -> EntityConvertToDTOUtil.convertToDTO(voucher, VoucherDTO.class))
				.collect(Collectors.toList());

		Page<VoucherDTO> voucherDTOPage = new PageImpl<>(voucherDTOs, pageable, vouchers.getTotalElements());

		return ResponseEntity.ok(voucherDTOPage);
	}

	@Operation(summary = "Get Voucher details based on Voucher ID", description = "Get detailed information of a Voucher based on the voucher ID")
	@GetMapping("/vouchers/{id}")
	public ResponseEntity<VoucherDTO> getVoucherById(Authentication authentication, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		VoucherDTO voucherSavedDTO = voucherService.getVoucherById(id);
		return ResponseEntity.ok(voucherSavedDTO);
	}

	@Operation(summary = "Update Voucher details based on voucher ID", description = "Update the details of a Voucher based on the voucher ID")
	@PutMapping("/vouchers/{id}")
	public ResponseEntity<VoucherDTO> updateVoucherById(Authentication authentication, @PathVariable("id") Long id,
			@Valid @RequestBody VoucherDTO voucherDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		if (voucherDTO.getId() != null && !id.equals(voucherDTO.getId())) {
			throw new IllegalArgumentException("Đã xảy ra lỗi khi cập nhật Voucher.");
		}
		voucherDTO.setId(id);
		VoucherDTO updatedVoucherDTO = voucherService.updateVoucher(id, voucherDTO);
		return ResponseEntity.ok(updatedVoucherDTO);
	}

	@Operation(summary = "Delete a Voucher based on Voucher ID", description = "Delete a Voucher from the system based on the voucher ID")
	@DeleteMapping("/vouchers/{id}")
	public ResponseEntity<Void> deleteVoucherById(Authentication authentication, @PathVariable("id") Long id) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		voucherService.deleteVoucherById(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Get all deposit cards to top up", description = "Get all top-up deposit cards (including all statuses)")
	@GetMapping("/deposits/card")
	public ResponseEntity<Page<CardDepositTransactionDTO>> getAllCardDeposits(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "timeOfDepositing,desc") String sort,
			@RequestParam(required = false) CardDepositStatus cardDepositStatus,
			@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate,
			@RequestParam(required = false) Long minValue, @RequestParam(required = false) Long maxValue) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<CardDepositOrder> transactions = cardDepositService.getAllCardDepositOrders(pageable, cardDepositStatus,
				startDate, endDate, minValue, maxValue);

		if (page >= transactions.getTotalPages()) {
			return ResponseEntity
					.ok(new PageImpl<>(Collections.emptyList(), pageable, transactions.getTotalElements()));
		}

		List<CardDepositTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, CardDepositTransactionDTO.class))
				.collect(Collectors.toList());

		Page<CardDepositTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Get all top-up deposit cards with Success status", description = "Get all top-up deposit cards with Success status")
	@GetMapping("/deposits/card/success")
	public ResponseEntity<Page<CardDepositTransactionDTO>> getSuccessfulCardDeposits(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "timeOfDepositing,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<CardDepositOrder> cardDeposits = cardDepositService.getCardDepositOrdersByStatus(pageable,
				CardDepositStatus.SUCCESS);

		List<CardDepositTransactionDTO> cardDepositDTOs = cardDeposits.getContent().stream()
				.map(cardDeposit -> EntityConvertToDTOUtil.convertToDTO(cardDeposit, CardDepositTransactionDTO.class))
				.collect(Collectors.toList());

		Page<CardDepositTransactionDTO> cardDepositDTOPage = new PageImpl<>(cardDepositDTOs, pageable,
				cardDeposits.getTotalElements());

		return ResponseEntity.ok(cardDepositDTOPage);
	}

	@Operation(summary = "Approve deposit card and add money to user", description = "Approve (Accept) the user's top-up deposit card and proceed to add money to the user according to the deposit card face value")
	@PatchMapping("/deposits/card/{cardId}/approve")
	public ResponseEntity<CardDepositApprovalDTO> approveCard(Authentication authentication,
			@PathVariable("cardId") Long cardId) {

		User admin = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(admin);

		cardDepositService.approveCard(cardId);

		CardDepositApprovalDTO response = new CardDepositApprovalDTO();
		response.setMessage("Duyệt thẻ thành công");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Reject user's deposit card", description = "Reject the user's recharge card because the card is wrong, invalid,...")
	@PatchMapping("/deposits/card/{cardId}/reject")
	public ResponseEntity<CardDepositApprovalDTO> rejectCard(Authentication authentication,
			@PathVariable("cardId") Long cardId) {

		User admin = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(admin);

		cardDepositService.rejectCard(cardId);

		CardDepositApprovalDTO response = new CardDepositApprovalDTO();
		response.setMessage("Từ chối thẻ thành công");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get all VNPay deposit transactions", description = "Get all deposit transactions via VNPay method (Successful status)")
	@GetMapping("/deposits/vnpay")
	public ResponseEntity<Page<VNPayTransactionDTO>> getAllVNPayDeposits(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "timeOfDepositing,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<VNPayTransaction> transactions = vnPayTransactionService.getVNPayDepositOrders(pageable);

		List<VNPayTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, VNPayTransactionDTO.class))
				.collect(Collectors.toList());

		Page<VNPayTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Get all Game account transactions", description = "Get all Game account purchases on the system (Account purchase history of all users on the system)")
	@GetMapping("/transactions/accounts")
	public ResponseEntity<Page<GameAccountTransactionDTO>> getAllAccountTransactions(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "transactionDate,desc") String sort) {

		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);

		String[] sortParams = sort.split(",");
		String sortField = sortParams[0];
		Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		Page<GameAccountTransaction> transactions = gameAccountTransactionService.getAllAccountTransactions(pageable);

		List<GameAccountTransactionDTO> transactionDTOs = transactions.getContent().stream()
				.map(transaction -> EntityConvertToDTOUtil.convertToDTO(transaction, GameAccountTransactionDTO.class))
				.collect(Collectors.toList());

		Page<GameAccountTransactionDTO> transactionDTOPage = new PageImpl<>(transactionDTOs, pageable,
				transactions.getTotalElements());

		return ResponseEntity.ok(transactionDTOPage);
	}

	@Operation(summary = "Get information related to deposit", description = "Get detailed information about deposit methods on the system such as conversion fees and QR codes")
	@GetMapping("/deposit-payment-info")
	public ResponseEntity<DepositPaymentInfoDTO> getDepositPaymentInfo(Authentication authentication) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		DepositPaymentInfoDTO depositPaymentInfoDTO = depositPaymentInfoService.getDepositPaymentInfo();
		return ResponseEntity.ok(depositPaymentInfoDTO);
	}

	@Operation(summary = "Update information related to deposit", description = "Update detailed information related to deposit methods on the system such as conversion fees and QR Codes")
	@PutMapping("/deposit-payment-info")
	public ResponseEntity<DepositPaymentInfoDTO> updateDepositPaymentInfo(Authentication authentication,
			@Valid @RequestBody DepositPaymentInfoDTO depositPaymentInfoDTO) {
		User user = AuthenticationUtil.getUserFromAuthentication(authentication);
		checkPermissions(user);
		DepositPaymentInfoDTO updatedDepositPaymentInfoDTO = depositPaymentInfoService
				.updateDepositPaymentInfo(depositPaymentInfoDTO);
		return ResponseEntity.ok(updatedDepositPaymentInfoDTO);
	}

	private void checkPermissions(User user) {
		if (user.getUserStatus() == UserStatus.LOCKED) {
			throw new ForbiddenException("Tài khoản của bạn đã bị khóa, không thể sử dụng tính năng này!");
		}
		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException("Bạn không được phép sử dụng tính năng này.");
		}
	}
}