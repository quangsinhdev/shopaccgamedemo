package com.shopaccgame.repositories.gameaccount;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shopaccgame.dtos.gameaccount.LOLAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.models.gameaccount.lol.LOLAccount;

@Repository
public interface LOLAccountRepository extends JpaRepository<LOLAccount, Long> {
	LOLAccount save(LOLAccountDTO lolAccountDTO);

	Optional<LOLAccount> findById(Long id);

	Optional<LOLAccount> findByUsername(String username);

	Page<LOLAccount> findAll(Pageable pageable);

	Page<LOLAccount> findByGameAccountStatus(Pageable pageable, GameAccountStatus gameAccountStatus);

	@Query("SELECT acclmht FROM LOLAccount acclmht WHERE acclmht.gameAccountStatus = 'SELLING'")
	Page<LOLAccount> findAllSelling(Pageable pageable);

	void deleteById(Long id);

	boolean existsById(Long id);

	boolean existsByUsername(String username);

	long count();
}
