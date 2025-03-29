package com.shopaccgame.services.gameaccount;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.gameaccount.LOLAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotFoundException;
import com.shopaccgame.models.gameaccount.lol.LOLAccount;
import com.shopaccgame.repositories.gameaccount.LOLAccountRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class LOLService {
	private final LOLAccountRepository lolAccountRepository;

	public LOLService(LOLAccountRepository lolAccountRepository) {
		this.lolAccountRepository = lolAccountRepository;
	}

	public Page<LOLAccount> getAllLOLAccounts(Pageable pageable) {
		return lolAccountRepository.findAll(pageable);
	}

	public Page<LOLAccount> getAllLOLAccountsSelling(Pageable pageable) {
		return lolAccountRepository.findAllSelling(pageable);
	}

	public LOLAccount getLOLAccountById(Long id) {
		return lolAccountRepository.findById(id).orElseThrow(
				() -> new GameAccountNotFoundException("Account LOL cần tìm không tồn tại", HttpStatus.NOT_FOUND));
	}

	public Page<LOLAccount> getLOLAccountsByStatus(Pageable pageable, GameAccountStatus gameAccountStatus) {
		return lolAccountRepository.findByGameAccountStatus(pageable, gameAccountStatus);
	}

	@Transactional
	public LOLAccountDTO createLOLAccount(LOLAccountDTO lolAccountDTO) {

		if (lolAccountRepository.existsByUsername(lolAccountDTO.getUsername())) {
			throw new IllegalArgumentException("Tài khoản Game đã tồn tại: " + lolAccountDTO.getUsername());
		}

		LOLAccount lolAccount = DTOConvertToEntityUtil.convertToEntity(lolAccountDTO, LOLAccount.class);

		if (lolAccount.getGameAccountStatus() == null) {
			lolAccount.setGameAccountStatus(GameAccountStatus.SELLING);
		}
		LOLAccount lolAccountSaved = lolAccountRepository.save(lolAccount);

		return EntityConvertToDTOUtil.convertToDTO(lolAccountSaved, LOLAccountDTO.class);
	}

	@Transactional
	public LOLAccountDTO updateLOLAccount(LOLAccountDTO lolAccountDTO) {
		LOLAccount existingAccount = lolAccountRepository.findById(lolAccountDTO.getId()).orElseThrow(
				() -> new GameAccountNotFoundException("Không tìm thấy tài khoản LOL với ID: " + lolAccountDTO.getId(),
						HttpStatus.NOT_FOUND));

		LOLAccount updatedAccount = DTOConvertToEntityUtil.convertToEntity(lolAccountDTO, LOLAccount.class);
		updatedAccount.setId(existingAccount.getId());

		LOLAccount savedAccount = lolAccountRepository.save(updatedAccount);
		return EntityConvertToDTOUtil.convertToDTO(savedAccount, LOLAccountDTO.class);
	}

	public void deleteLOLAccountById(Long id) {
		lolAccountRepository.deleteById(id);
	}

}