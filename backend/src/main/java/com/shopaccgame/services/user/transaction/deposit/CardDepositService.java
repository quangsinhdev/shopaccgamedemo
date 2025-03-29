package com.shopaccgame.services.user.transaction.deposit;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.transaction.deposit.CardDepositRequestDTO;
import com.shopaccgame.dtos.transaction.deposit.CardDepositResponseDTO;
import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;
import com.shopaccgame.exceptions.transaction.deposit.CardDepositException;
import com.shopaccgame.exceptions.transaction.deposit.DepositPaymentInfoException;
import com.shopaccgame.models.transaction.deposit.CardDepositOrder;
import com.shopaccgame.models.transaction.deposit.DepositPaymentInfo;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.transaction.deposit.CardDepositRepository;
import com.shopaccgame.repositories.transaction.deposit.DepositPaymentInfoRepository;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class CardDepositService {
	private final CardDepositRepository cardDepositRepository;
	private final DepositPaymentInfoRepository depositPaymentInfoRepository;

	public CardDepositService(CardDepositRepository cardDepositRepository,
			DepositPaymentInfoRepository depositPaymentInfoRepository) {
		this.cardDepositRepository = cardDepositRepository;
		this.depositPaymentInfoRepository = depositPaymentInfoRepository;
	}

	@Transactional
	public CardDepositResponseDTO submitDepositCard(CardDepositRequestDTO cardDepositRequestDTO, User user) {
		if (cardDepositRepository.existsByDepositCardNetworkProviderAndSerialAndCodeAndValue(
				cardDepositRequestDTO.getDepositCardNetworkProvider(), cardDepositRequestDTO.getSerial(),
				cardDepositRequestDTO.getCode(), cardDepositRequestDTO.getValue())) {
			throw new CardDepositException("Thẻ đã tồn tại trên hệ thống. Vui lòng đợi xử lý");
		}
		if (cardDepositRequestDTO.getDepositCardNetworkProvider() == null) {
			throw new CardDepositException("Nhà mạng không được để trống");
		}
		if (cardDepositRequestDTO.getSerial() == null || cardDepositRequestDTO.getSerial().trim().isEmpty()) {
			throw new CardDepositException("Serial không được để trống");
		}
		if (cardDepositRequestDTO.getCode() == null || cardDepositRequestDTO.getCode().trim().isEmpty()) {
			throw new CardDepositException("Mã thẻ không được để trống");
		}
		if (cardDepositRequestDTO.getValue() <= 0) {
			throw new CardDepositException("Giá trị thẻ phải lớn hơn 0");
		}
		DepositPaymentInfo depositPaymentInfo = depositPaymentInfoRepository.findById(1L)
				.orElseThrow(() -> new DepositPaymentInfoException("Không tìm thấy các thông tin về Thanh toán"));

		CardDepositOrder newCardDepositOrder = new CardDepositOrder();
		newCardDepositOrder.setDepositCardNetworkProvider(cardDepositRequestDTO.getDepositCardNetworkProvider());
		newCardDepositOrder.setSerial(cardDepositRequestDTO.getSerial());
		newCardDepositOrder.setCode(cardDepositRequestDTO.getCode());
		newCardDepositOrder.setValue(cardDepositRequestDTO.getValue());
		newCardDepositOrder.setActuallyReceive(calculateActuallyReceive(cardDepositRequestDTO.getValue(),
				getTradeCost(depositPaymentInfo, cardDepositRequestDTO.getDepositCardNetworkProvider())));
		newCardDepositOrder.setCardDepositStatus(CardDepositStatus.PENDING);
		newCardDepositOrder.setTimeOfDepositing(LocalDateTime.now());
		newCardDepositOrder.setUser(user);
		newCardDepositOrder.setDepositorUsername(user.getUsername());

		cardDepositRepository.save(newCardDepositOrder);
		return EntityConvertToDTOUtil.convertToDTO(newCardDepositOrder, CardDepositResponseDTO.class);

	}

	@Transactional
	public void approveCard(Long cardId) {
		CardDepositOrder card = cardDepositRepository.findById(cardId).orElseThrow(() -> {
			return new CardDepositException("Không tìm thông tin thẻ cào cần phê duyệt", HttpStatus.NOT_FOUND);
		});

		if (!card.getCardDepositStatus().equals(CardDepositStatus.PENDING)) {
			throw new CardDepositException("Thẻ đã được xử lý (trạng thái: " + card.getCardDepositStatus() + ")",
					HttpStatus.CONFLICT);
		}

		User user = card.getUser();
		if (user == null) {
			throw new CardDepositException("Không tìm thấy người dùng liên quan đến thẻ",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Long actuallyReceive = card.getActuallyReceive();
		if (actuallyReceive == null || actuallyReceive < 0) {
			throw new CardDepositException("Giá trị thực nhận của thẻ không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		long currentUserBalance = user.getBalance();
		long newBalance = currentUserBalance + actuallyReceive;
		if (newBalance < 0) {
			throw new CardDepositException("Số dư sau khi cộng không hợp lệ (số dư âm)", HttpStatus.BAD_REQUEST);
		}

		user.setBalance(newBalance);
		card.setCardDepositStatus(CardDepositStatus.SUCCESS);

	}

	@Transactional
	public void rejectCard(Long cardId) {
		CardDepositOrder card = cardDepositRepository.findById(cardId).orElseThrow(() -> {
			return new CardDepositException("Không tìm thông tin thẻ cào cần từ chối", HttpStatus.NOT_FOUND);
		});

		if (!card.getCardDepositStatus().equals(CardDepositStatus.PENDING)) {
			throw new CardDepositException("Thẻ đã được xử lý (trạng thái: " + card.getCardDepositStatus() + ")",
					HttpStatus.CONFLICT);
		}

		card.setCardDepositStatus(CardDepositStatus.REJECTED);

	}

	public Page<CardDepositOrder> getCardDepositOrderByUser(Pageable pageable, User user) {
		return cardDepositRepository.findByUser(pageable, user);
	}

	public Page<CardDepositOrder> getAllCardDepositOrders(Pageable pageable, CardDepositStatus cardDepositStatus,
			String startDate, String endDate, Long minValue, Long maxValue) {
		return cardDepositRepository.findFilteredCardDeposits(pageable, cardDepositStatus, startDate, endDate, minValue,
				maxValue);
	}

	public Page<CardDepositOrder> getCardDepositOrdersByStatus(Pageable pageable, CardDepositStatus cardDepositStatus) {
		return cardDepositRepository.findByCardDepositStatus(pageable, cardDepositStatus);
	}

	private static double getTradeCost(DepositPaymentInfo depositPaymentInfo, DepositCardNetworkProvider provider) {
		switch (provider) {
		case VIETTEL:
			return depositPaymentInfo.getViettelTradecost() / 100.0;
		case MOBIFONE:
			return depositPaymentInfo.getMobifoneTradecost() / 100.0;
		case VINAPHONE:
			return depositPaymentInfo.getVinaphoneTradecost() / 100.0;
		default:
			throw new CardDepositException("Nhà mạng: " + provider + " không được hỗ trợ.");
		}
	}

	private static long calculateActuallyReceive(long value, double tradeCost) {
		return (long) (value - (value * tradeCost));
	}
}
