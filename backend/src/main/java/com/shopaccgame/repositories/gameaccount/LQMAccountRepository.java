package com.shopaccgame.repositories.gameaccount;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shopaccgame.dtos.gameaccount.LQMAccountDTO;
import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.models.gameaccount.lqm.LQMAccount;

@Repository
public interface LQMAccountRepository extends JpaRepository<LQMAccount, Long> {
	LQMAccount save(LQMAccountDTO lqmAccountDTO);

	Optional<LQMAccount> findById(Long id);

	Optional<LQMAccount> findByUsername(String username);

	Page<LQMAccount> findAll(Pageable pageable);

	Page<LQMAccount> findByGameAccountStatus(Pageable pageable, GameAccountStatus gameAccountStatus);

	@Query("SELECT acclqm FROM LQMAccount acclqm WHERE acclqm.gameAccountStatus = 'SELLING'")
	Page<LQMAccount> findAllSelling(Pageable pageable);

	void deleteById(Long id);

	boolean existsById(Long id);

	boolean existsByUsername(String username);

	long count();
}
