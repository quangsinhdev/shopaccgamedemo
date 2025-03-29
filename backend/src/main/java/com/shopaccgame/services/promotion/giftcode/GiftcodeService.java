package com.shopaccgame.services.promotion.giftcode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.promotion.giftcode.GiftcodeDTO;
import com.shopaccgame.dtos.promotion.giftcode.GiftcodeResponseDTO;
import com.shopaccgame.enums.promotion.PromotionStatus;
import com.shopaccgame.exceptions.promotion.giftcode.GiftcodeNotAvailableException;
import com.shopaccgame.exceptions.promotion.giftcode.GiftcodeNotFoundException;
import com.shopaccgame.exceptions.user.UserNotFoundException;
import com.shopaccgame.models.promotion.giftcode.Giftcode;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.promotion.giftcode.GiftcodeRepository;
import com.shopaccgame.repositories.user.UserRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class GiftcodeService {
	private final GiftcodeRepository giftcodeRepository;
	private final UserRepository userRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	public GiftcodeService(GiftcodeRepository giftcodeRepository, UserRepository userRepository,
			RedisTemplate<String, Object> redisTemplate) {
		this.giftcodeRepository = giftcodeRepository;
		this.userRepository = userRepository;
		this.redisTemplate = redisTemplate;
	}

	public Page<Giftcode> getAllGiftcodes(Pageable pageable) {
		return giftcodeRepository.findAll(pageable);
	}

	public GiftcodeDTO getGiftcodeById(Long id) {
		Giftcode giftcode = giftcodeRepository.findById(id)
				.orElseThrow(() -> new GiftcodeNotFoundException("Giftcode không tồn tại", HttpStatus.NOT_FOUND));
		return EntityConvertToDTOUtil.convertToDTO(giftcode, GiftcodeDTO.class);
	}

	public GiftcodeDTO saveGiftcode(GiftcodeDTO giftcodeDTO) {
		Giftcode giftcode = DTOConvertToEntityUtil.convertToEntity(giftcodeDTO, Giftcode.class);
		Giftcode giftcodeSaved = giftcodeRepository.save(giftcode);
		return EntityConvertToDTOUtil.convertToDTO(giftcodeSaved, GiftcodeDTO.class);
	}

	@Transactional
	public GiftcodeDTO createGiftcode(GiftcodeDTO giftcodeDTO) {
		if (giftcodeDTO.getCode() == null || giftcodeDTO.getCode().trim().isEmpty()) {
			throw new IllegalArgumentException("Mã giftcode không được để trống");
		}

		if (giftcodeRepository.existsByCode(giftcodeDTO.getCode())) {
			throw new IllegalArgumentException("Mã giftcode đã tồn tại: " + giftcodeDTO.getCode());
		}

		Giftcode giftcode = DTOConvertToEntityUtil.convertToEntity(giftcodeDTO, Giftcode.class);

		giftcode.setPromotionStatus(PromotionStatus.AVAILABLE);

		Giftcode savedGiftcode = giftcodeRepository.save(giftcode);

		return EntityConvertToDTOUtil.convertToDTO(savedGiftcode, GiftcodeDTO.class);
	}

	@Transactional
	public GiftcodeDTO updateGiftcode(Long id, GiftcodeDTO giftcodeDTO) {
		Giftcode existingGiftcode = giftcodeRepository.findById(id).orElseThrow(
				() -> new GiftcodeNotFoundException("Giftcode không tồn tại với ID: " + id, HttpStatus.NOT_FOUND));

		Giftcode updatedGiftcode = DTOConvertToEntityUtil.convertToEntity(giftcodeDTO, Giftcode.class);
		updatedGiftcode.setId(id);

		updatedGiftcode.setCode(existingGiftcode.getCode());

		Giftcode savedGiftcode = giftcodeRepository.save(updatedGiftcode);
		return EntityConvertToDTOUtil.convertToDTO(savedGiftcode, GiftcodeDTO.class);
	}

	@Transactional
	public GiftcodeResponseDTO receiveGiftcode(Long userId, String code) {

		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã giftcode không được để trống");
		}

		String lockKey = "lock:giftcode:" + code;
		Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
		if (Boolean.FALSE.equals(locked)) {
			throw new GiftcodeNotAvailableException("Giftcode đang được xử lý, thử lại sau!", HttpStatus.CONFLICT);
		}

		try {
			Giftcode giftcode = giftcodeRepository.findByCode(code).orElseThrow(() -> {
				return new GiftcodeNotFoundException("Giftcode không tồn tại", HttpStatus.NOT_FOUND);
			});

			if (giftcode.getPromotionStatus() != PromotionStatus.AVAILABLE) {
				throw new GiftcodeNotAvailableException("Giftcode đã được sử dụng hoặc không khả dụng",
						HttpStatus.CONFLICT);
			}

			User user = userRepository.findById(userId).orElseThrow(() -> {
				return new UserNotFoundException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
			});

			long oldBalance = user.getBalance();
			user.setBalance(oldBalance + giftcode.getValue());

			giftcode.setPromotionStatus(PromotionStatus.USED);
			giftcode.setTimeOfUse(LocalDateTime.now());

			GiftcodeResponseDTO responseDTO = EntityConvertToDTOUtil.convertToDTO(giftcode, GiftcodeResponseDTO.class);
			responseDTO.setMessageResponse("Nhận quà Giftcode thành công! Giá trị: " + giftcode.getValue());
			return responseDTO;

		} finally {
			redisTemplate.delete(lockKey);
		}
	}

	public void deleteGiftcodeById(Long id) {
		giftcodeRepository.deleteById(id);
	}
}