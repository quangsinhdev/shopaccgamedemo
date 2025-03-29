package com.shopaccgame.repositories.transaction.gameaccount;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.transaction.gameaccount.GameAccountTransaction;
import com.shopaccgame.models.user.User;

@Repository
public interface GameAccountTransactionRepository extends JpaRepository<GameAccountTransaction, Long> {
	GameAccountTransaction save(GameAccountTransaction accountTransaction);

	Optional<GameAccountTransaction> findById(Long id);

	Page<GameAccountTransaction> findAll(Pageable pageable);

	Page<GameAccountTransaction> findByUser(Pageable pageable, User user);

	List<GameAccountTransaction> findByUserId(Long id);

	void deleteById(Long id);

	long count();
}
