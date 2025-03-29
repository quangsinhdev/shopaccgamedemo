package com.shopaccgame.services.user.transaction.gameaccount;

import com.shopaccgame.dtos.transaction.purchase.PurchaseResponseDTO;
import com.shopaccgame.dtos.user.UserDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.enums.gameaccount.GameAccountType;
import com.shopaccgame.enums.promotion.PromotionStatus;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotAvailableException;
import com.shopaccgame.exceptions.transaction.gameaccount.GameAccountTransactionException;
import com.shopaccgame.models.gameaccount.GameAccount;
import com.shopaccgame.models.promotion.voucher.Voucher;
import com.shopaccgame.models.transaction.gameaccount.GameAccountTransaction;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.gameaccount.GameAccountRepository;
import com.shopaccgame.repositories.promotion.voucher.VoucherRepository;
import com.shopaccgame.repositories.transaction.gameaccount.GameAccountTransactionRepository;
import com.shopaccgame.repositories.user.UserRepository;
import com.shopaccgame.services.gameaccount.FifaService;
import com.shopaccgame.services.gameaccount.LOLService;
import com.shopaccgame.services.gameaccount.LQMService;
import com.shopaccgame.services.promotion.voucher.VoucherService;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class GameAccountTransactionService {
	private final GameAccountTransactionRepository gameAccountTransactionRepository;
	private final FifaService fifaService;
	private final LOLService lolService;
	private final LQMService lqmService;
	private final UserRepository userRepository;
	private final GameAccountRepository gameAccountRepository;
	private final VoucherService voucherService;
	private final VoucherRepository voucherRepository;
	private RedisTemplate<String, Object> redisTemplate;

	public GameAccountTransactionService(GameAccountTransactionRepository gameAccountTransactionRepository,
			FifaService fifaService, LOLService lolService, LQMService lqmService, UserRepository userRepository,
			GameAccountRepository gameAccountRepository, VoucherService voucherService,
			VoucherRepository voucherRepository, RedisTemplate<String, Object> redisTemplate) {
		this.gameAccountTransactionRepository = gameAccountTransactionRepository;
		this.fifaService = fifaService;
		this.lolService = lolService;
		this.lqmService = lqmService;
		this.userRepository = userRepository;
		this.gameAccountRepository = gameAccountRepository;
		this.voucherService = voucherService;
		this.voucherRepository = voucherRepository;
		this.redisTemplate = redisTemplate;
	}

	@Transactional
	public PurchaseResponseDTO purchaseAccount(User user, Long accountId, GameAccountType gameAccountType,
			String voucherCode) {
		String accountLockKey = "lock:account:" + gameAccountType.name().toLowerCase() + ":" + accountId;
		String voucherLockKey = (voucherCode != null && !voucherCode.isEmpty()) ? "lock:voucher:" + voucherCode : null;

		Boolean accountLocked = redisTemplate.opsForValue().setIfAbsent(accountLockKey, "locked", 10, TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(accountLocked)) {
			throw new GameAccountNotAvailableException("Tài khoản đang được xử lý, thử lại sau!");
		}
		boolean voucherLocked = true;
		if (voucherLockKey != null) {
			voucherLocked = Boolean.TRUE
					.equals(redisTemplate.opsForValue().setIfAbsent(voucherLockKey, "locked", 10, TimeUnit.SECONDS));
			if (!voucherLocked) {
				redisTemplate.delete(accountLockKey);
				throw new GameAccountNotAvailableException("Voucher đang được xử lý, thử lại sau!");
			}
		}

		try {
			GameAccount account = getAccountByTypeAndId(gameAccountType, accountId);
			if (account == null || account.getGameAccountStatus().equals(GameAccountStatus.SOLD)) {
				throw new GameAccountNotAvailableException("Tài khoản không tồn tại hoặc đã được bán.");
			}

			long paymentAmount = (long) (account.getPrice() * (1 - account.getDiscount() / 100.0));

			Voucher voucher = null;
			if (voucherCode != null && !voucherCode.isEmpty()) {
				voucher = voucherService.applyVoucher(voucherCode);
				paymentAmount = Math.max(paymentAmount - voucher.getValue(), 0);
			}

			if (user.getBalance() < paymentAmount) {
				throw new GameAccountTransactionException("Số dư không đủ để mua tài khoản. Vui lòng nạp tiền thêm.");
			}

			user.setBalance(user.getBalance() - paymentAmount);
			userRepository.save(user);

			account.setGameAccountStatus(GameAccountStatus.SOLD);
			gameAccountRepository.save(account);

			if (voucher != null) {
				voucher.setPromotionStatus(PromotionStatus.USED);
				voucher.setTimeOfUse(LocalDateTime.now());
				voucherRepository.save(voucher);
			}

			GameAccountTransaction transaction = new GameAccountTransaction();
			transaction.setAccountId(accountId);
			transaction.setGameAccountType(gameAccountType);
			transaction.setPrice(paymentAmount);
			transaction.setUser(user);
			transaction.setTransactorUsername(user.getUsername());
			transaction.setTransactionDate(LocalDateTime.now());
			transaction.setUsernameAccount(account.getUsername());
			transaction.setPasswordAccount(account.getPassword());
			transaction.setEmailAccount(account.getEmail());
			transaction.setPhoneNumberAccount(account.getPhonenumber());
			transaction.setAccountDescription(account.getDescription());
			gameAccountTransactionRepository.save(transaction);

			PurchaseResponseDTO purchaseResponseDTO = EntityConvertToDTOUtil.convertToDTO(transaction,
					PurchaseResponseDTO.class);
			UserDTO userDTO = EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
			purchaseResponseDTO.setUser(userDTO);
			return purchaseResponseDTO;

		} finally {
			redisTemplate.delete(accountLockKey);
			if (voucherLockKey != null) {
				redisTemplate.delete(voucherLockKey);
			}
		}
	}

	public Page<GameAccountTransaction> getPurchaseHistory(Pageable pageable, User user) {
		return gameAccountTransactionRepository.findByUser(pageable, user);
	}

	public Page<GameAccountTransaction> getAllAccountTransactions(Pageable pageable) {
		return gameAccountTransactionRepository.findAll(pageable);
	}

	private GameAccount getAccountByTypeAndId(GameAccountType gameAccountType, Long accountId) {
		switch (gameAccountType) {
		case FIFA:
			return fifaService.getFifaAccountById(accountId);
		case LOL:
			return lolService.getLOLAccountById(accountId);
		case LQ:
			return lqmService.getLQMAccountById(accountId);
		default:
			return null;
		}
	}
}