package com.shopaccgame.repositories.gameaccount;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shopaccgame.dtos.gameaccount.FifaAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.models.gameaccount.fifa.FifaAccount;

@Repository
public interface FifaAccountRepository extends JpaRepository<FifaAccount, Long> {
	FifaAccount save(FifaAccountDTO fifaAccountDTO);

	Optional<FifaAccount> findById(Long id);

	Optional<FifaAccount> findByUsername(String username);

	Page<FifaAccount> findAll(Pageable pageable);

	Page<FifaAccount> findByGameAccountStatus(Pageable pageable, GameAccountStatus gameAccountStatus);

	@Query("SELECT accfifa FROM FifaAccount accfifa WHERE accfifa.gameAccountStatus = 'SELLING'")
	Page<FifaAccount> findAllSelling(Pageable pageable);

	void deleteById(Long id);

	boolean existsById(Long id);

	boolean existsByUsername(String username);

	long count();
}
