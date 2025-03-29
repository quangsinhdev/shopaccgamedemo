package com.shopaccgame.services.promotion.voucher;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;
import com.shopaccgame.models.promotion.voucher.Voucher;
import com.shopaccgame.repositories.promotion.voucher.VoucherRepository;
import com.shopaccgame.dtos.promotion.voucher.VoucherDTO;
import com.shopaccgame.enums.promotion.PromotionStatus;
import com.shopaccgame.exceptions.promotion.voucher.VoucherNotAvailableException;
import com.shopaccgame.exceptions.promotion.voucher.VoucherNotFoundException;

@Service
public class VoucherService {
	private final VoucherRepository voucherRepository;

	public VoucherService(VoucherRepository voucherRepository) {
		this.voucherRepository = voucherRepository;
	}

	public Page<Voucher> getAllVouchers(Pageable pageable) {
		return voucherRepository.findAll(pageable);
	}

	public VoucherDTO getVoucherById(Long id) {
		Voucher voucher =  voucherRepository.findById(id).orElseThrow(() -> new VoucherNotFoundException("Voucher không tồn tại", HttpStatus.NOT_FOUND));
		return EntityConvertToDTOUtil.convertToDTO(voucher, VoucherDTO.class);
	}

	@Transactional
    public VoucherDTO updateVoucher(Long id, VoucherDTO voucherDTO) {
        Voucher existingVoucher = voucherRepository.findById(id)
                .orElseThrow(() -> new VoucherNotFoundException("Không tìm thấy voucher với ID: " + id, HttpStatus.NOT_FOUND));

        existingVoucher = DTOConvertToEntityUtil.convertToEntity(voucherDTO, Voucher.class);
        existingVoucher.setId(id);
        voucherRepository.save(existingVoucher);
        return EntityConvertToDTOUtil.convertToDTO(existingVoucher, VoucherDTO.class);
    }
	
	@Transactional
	public VoucherDTO createVoucher(VoucherDTO voucherDTO) {
		if (voucherDTO.getCode() == null || voucherDTO.getCode().trim().isEmpty()) {
			throw new IllegalArgumentException("Mã voucher không được để trống");
		}

		if (voucherRepository.existsByCode(voucherDTO.getCode())) {
			throw new IllegalArgumentException("Mã voucher đã tồn tại: " + voucherDTO.getCode());
		}

		if (voucherDTO.getVoucherExpireDate().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Ngày hết hạn của voucher phải là hiện tại hoặc tương lai");
		}

		Voucher voucher = DTOConvertToEntityUtil.convertToEntity(voucherDTO, Voucher.class);

		voucher.setPromotionStatus(PromotionStatus.AVAILABLE);

		Voucher savedVoucher = voucherRepository.save(voucher);

		return EntityConvertToDTOUtil.convertToDTO(savedVoucher, VoucherDTO.class);
	}

	@Transactional
	public Voucher applyVoucher(String code) {
		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("Mã voucher không được để trống");
		}

		Voucher voucher = voucherRepository.findByCode(code)
				.orElseThrow(() -> new VoucherNotFoundException("Voucher không tồn tại", HttpStatus.NOT_FOUND));

		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(voucher.getVoucherExpireDate())) {
			throw new VoucherNotAvailableException("Voucher đã hết hạn", HttpStatus.CONFLICT);
		}
		if (voucher.getPromotionStatus() == PromotionStatus.USED) {
			throw new VoucherNotAvailableException("Voucher đã được sử dụng", HttpStatus.CONFLICT);
		}
		return voucher;
	}
	
	public void deleteVoucherById(Long id) {
		voucherRepository.deleteById(id);
	}

}
