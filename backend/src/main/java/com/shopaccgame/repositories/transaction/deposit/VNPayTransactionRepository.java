package com.shopaccgame.repositories.transaction.deposit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.transaction.deposit.VNPayTransaction;
import com.shopaccgame.models.user.User;

@Repository
public interface VNPayTransactionRepository extends JpaRepository<VNPayTransaction, Long> {
	boolean existsByTxnRef(String txnRef);

	Page<VNPayTransaction> findByUser(Pageable pageable, User user);

	Page<VNPayTransaction> findAll(Pageable pageable);
}
