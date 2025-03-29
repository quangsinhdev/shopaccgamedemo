package com.shopaccgame.services.gameaccount;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.gameaccount.LQMAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotFoundException;
import com.shopaccgame.models.gameaccount.lqm.LQMAccount;
import com.shopaccgame.repositories.gameaccount.LQMAccountRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class LQMService {
	private final LQMAccountRepository lqmAccountRepository;

	public LQMService(LQMAccountRepository lqmAccountRepository) {
		this.lqmAccountRepository = lqmAccountRepository;
	}

	public Page<LQMAccount> getAllLQMAccounts(Pageable pageable) {
		return lqmAccountRepository.findAll(pageable);
	}

	public Page<LQMAccount> getAllLQMAccountsSelling(Pageable pageable) {
		return lqmAccountRepository.findAllSelling(pageable);
	}

	public LQMAccount getLQMAccountById(Long id) {
		return lqmAccountRepository.findById(id)
				.orElseThrow(() -> new GameAccountNotFoundException("Account Liên quân cần tìm không tồn tại",
						HttpStatus.NOT_FOUND));
	}

	public Page<LQMAccount> getLQMAccountsByStatus(Pageable pageable, GameAccountStatus gameAccountStatus) {
		return lqmAccountRepository.findByGameAccountStatus(pageable, gameAccountStatus);
	}

	@Transactional
	public LQMAccountDTO createLQMAccount(LQMAccountDTO lqmAccountDTO) {

		if (lqmAccountRepository.existsByUsername(lqmAccountDTO.getUsername())) {
			throw new IllegalArgumentException("Tài khoản Game đã tồn tại: " + lqmAccountDTO.getUsername());
		}

		LQMAccount lqmAccount = DTOConvertToEntityUtil.convertToEntity(lqmAccountDTO, LQMAccount.class);

		if (lqmAccount.getGameAccountStatus() == null) {
			lqmAccount.setGameAccountStatus(GameAccountStatus.SELLING);
		}
		LQMAccount lqmAccountSaved = lqmAccountRepository.save(lqmAccount);

		return EntityConvertToDTOUtil.convertToDTO(lqmAccountSaved, LQMAccountDTO.class);
	}

	@Transactional
	public LQMAccountDTO updateLQMAccount(LQMAccountDTO lqmAccountDTO) {
		LQMAccount existingAccount = lqmAccountRepository.findById(lqmAccountDTO.getId()).orElseThrow(
				() -> new GameAccountNotFoundException("Không tìm thấy tài khoản LOL với ID: " + lqmAccountDTO.getId(),
						HttpStatus.NOT_FOUND));

		LQMAccount updatedAccount = DTOConvertToEntityUtil.convertToEntity(lqmAccountDTO, LQMAccount.class);
		updatedAccount.setId(existingAccount.getId());

		LQMAccount savedAccount = lqmAccountRepository.save(updatedAccount);
		return EntityConvertToDTOUtil.convertToDTO(savedAccount, LQMAccountDTO.class);
	}

	public void deleteLQMAccountById(Long id) {
		lqmAccountRepository.deleteById(id);
	}

}