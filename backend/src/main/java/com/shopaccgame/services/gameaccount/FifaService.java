package com.shopaccgame.services.gameaccount;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.gameaccount.FifaAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.exceptions.gameaccount.GameAccountNotFoundException;
import com.shopaccgame.models.gameaccount.fifa.FifaAccount;
import com.shopaccgame.repositories.gameaccount.FifaAccountRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class FifaService {
    private final FifaAccountRepository fifaAccountRepository;

    public FifaService(FifaAccountRepository fifaAccountRepository) {
        this.fifaAccountRepository = fifaAccountRepository;
    }

    public Page<FifaAccount> getAllFifaAccounts(Pageable pageable) {
        return fifaAccountRepository.findAll(pageable);
    }

    public Page<FifaAccount> getAllFifaAccountsSelling(Pageable pageable) {
        return fifaAccountRepository.findAllSelling(pageable);
    }

    public FifaAccount getFifaAccountById(Long id) {
        return fifaAccountRepository.findById(id).orElseThrow(
                () -> new GameAccountNotFoundException("Account FIFA cần tìm không tồn tại", HttpStatus.NOT_FOUND));
    }

    public Page<FifaAccount> getFifaAccountsByStatus(Pageable pageable, GameAccountStatus gameAccountStatus) {
    	return fifaAccountRepository.findByGameAccountStatus(pageable, gameAccountStatus);
    }

    @Transactional
    public FifaAccountDTO createFifaAccount(FifaAccountDTO fifaAccountDTO) {
        if (fifaAccountRepository.existsByUsername(fifaAccountDTO.getUsername())) {
            throw new IllegalArgumentException("Tài khoản Game đã tồn tại: " + fifaAccountDTO.getUsername());
        }

        FifaAccount fifaAccount = DTOConvertToEntityUtil.convertToEntity(fifaAccountDTO, FifaAccount.class);

        if (fifaAccount.getGameAccountStatus() == null) {
            fifaAccount.setGameAccountStatus(GameAccountStatus.SELLING);
        }
        FifaAccount fifaAccountSaved = fifaAccountRepository.save(fifaAccount);

        return EntityConvertToDTOUtil.convertToDTO(fifaAccountSaved, FifaAccountDTO.class);
    }

    @Transactional
    public FifaAccountDTO updateFifaAccount(FifaAccountDTO fifaAccountDTO) {
        FifaAccount existingAccount = fifaAccountRepository.findById(fifaAccountDTO.getId())
                .orElseThrow(() -> new GameAccountNotFoundException(
                        "Không tìm thấy tài khoản FIFA với ID: " + fifaAccountDTO.getId(), HttpStatus.NOT_FOUND));

        FifaAccount updatedAccount = DTOConvertToEntityUtil.convertToEntity(fifaAccountDTO, FifaAccount.class);
        updatedAccount.setId(existingAccount.getId());

        FifaAccount savedAccount = fifaAccountRepository.save(updatedAccount);
        return EntityConvertToDTOUtil.convertToDTO(savedAccount, FifaAccountDTO.class);
    }

    public void deleteFifaAccountById(Long id) {
        fifaAccountRepository.deleteById(id);
    }
}