package com.shopaccgame.repositories.transaction.withdrawal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.enums.withdrawal.WithdrawalMethod;
import com.shopaccgame.enums.withdrawal.WithdrawalStatus;
import com.shopaccgame.models.transaction.withdrawal.WithdrawalOrder;
@Repository
public interface WithdrawalRepository extends JpaRepository<WithdrawalOrder, Long> {
	WithdrawalOrder save(WithdrawalOrder withdrawalOrder);

	Optional<WithdrawalOrder> findById(Long id);

	List<WithdrawalOrder> findAll();

	List<WithdrawalOrder> findByWithdrawalMethod(WithdrawalMethod withdrawalMethod);

	List<WithdrawalOrder> findByWithdrawStatus(WithdrawalStatus withdrawStatus);

	void deleteById(Long id);

	long count();
}
