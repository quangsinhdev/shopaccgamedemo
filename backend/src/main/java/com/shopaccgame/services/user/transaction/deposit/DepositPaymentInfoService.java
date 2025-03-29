package com.shopaccgame.services.user.transaction.deposit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.transaction.deposit.DepositPaymentInfoDTO;
import com.shopaccgame.exceptions.transaction.deposit.DepositPaymentInfoException;
import com.shopaccgame.models.transaction.deposit.DepositPaymentInfo;
import com.shopaccgame.repositories.transaction.deposit.DepositPaymentInfoRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class DepositPaymentInfoService {
	private final DepositPaymentInfoRepository depositPaymentInfoRepository;

	public DepositPaymentInfoService(DepositPaymentInfoRepository depositPaymentInfoRepository) {
		this.depositPaymentInfoRepository = depositPaymentInfoRepository;
	}

	@Transactional
	public DepositPaymentInfoDTO updateDepositPaymentInfo(DepositPaymentInfoDTO depositPaymentInfoDTO) {
		DepositPaymentInfo existingDepositPaymentInfo = depositPaymentInfoRepository.findById(1L)
				.orElseThrow(() -> new DepositPaymentInfoException("Không tìm thấy thông tin thanh toán nạp tiền."));

		DepositPaymentInfo updatedDepositPaymentInfo = DTOConvertToEntityUtil.convertToEntity(depositPaymentInfoDTO,
				DepositPaymentInfo.class);

		existingDepositPaymentInfo.setViettelTradecost(updatedDepositPaymentInfo.getViettelTradecost());
		existingDepositPaymentInfo.setMobifoneTradecost(updatedDepositPaymentInfo.getMobifoneTradecost());
		existingDepositPaymentInfo.setVinaphoneTradecost(updatedDepositPaymentInfo.getVinaphoneTradecost());
		existingDepositPaymentInfo.setQrCodeMomo(updatedDepositPaymentInfo.getQrCodeMomo());
		existingDepositPaymentInfo.setQrCodeViettelPay(updatedDepositPaymentInfo.getQrCodeViettelPay());

		return EntityConvertToDTOUtil.convertToDTO(existingDepositPaymentInfo, DepositPaymentInfoDTO.class);
	}

	public DepositPaymentInfoDTO getDepositPaymentInfo() {
		DepositPaymentInfo depositPaymentInfo = depositPaymentInfoRepository.findById(1L)
				.orElseThrow(() -> new DepositPaymentInfoException("Không tìm thấy các thông tin về Thanh toán"));
		return EntityConvertToDTOUtil.convertToDTO(depositPaymentInfo, DepositPaymentInfoDTO.class);
	}
}
