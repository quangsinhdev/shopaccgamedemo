package com.shopaccgame.repositories.transaction.deposit;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.transaction.deposit.DepositPaymentInfo;

@Repository
public interface DepositPaymentInfoRepository extends JpaRepository<DepositPaymentInfo, Long> {
	Optional<DepositPaymentInfo> findById(Long id);
}
