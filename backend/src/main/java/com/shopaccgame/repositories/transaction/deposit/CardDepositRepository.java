package com.shopaccgame.repositories.transaction.deposit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopaccgame.enums.deposit.CardDepositStatus;
import com.shopaccgame.enums.deposit.DepositCardNetworkProvider;
import com.shopaccgame.models.transaction.deposit.CardDepositOrder;
import com.shopaccgame.models.user.User;

@Repository
public interface CardDepositRepository extends JpaRepository<CardDepositOrder, Long> {
	Optional<CardDepositOrder> findBySerial(String serial);

	List<CardDepositOrder> findByCardDepositStatus(CardDepositStatus cardDepositStatus);

	Page<CardDepositOrder> findAll(Pageable pageable);

	Page<CardDepositOrder> findByUser(Pageable pageable, User user);

	Page<CardDepositOrder> findByCardDepositStatus(Pageable pageable, CardDepositStatus cardDepositStatus);

	@Query("SELECT c FROM CardDepositOrder c WHERE " + "(:status IS NULL OR c.cardDepositStatus = :status) AND "
			+ "(:startDate IS NULL OR c.timeOfDepositing >= :startDate) AND "
			+ "(:endDate IS NULL OR c.timeOfDepositing <= :endDate) AND "
			+ "(:minValue IS NULL OR c.value >= :minValue) AND " + "(:maxValue IS NULL OR c.value <= :maxValue)")
	Page<CardDepositOrder> findFilteredCardDeposits(Pageable pageable,
			@Param("status") CardDepositStatus cardDepositStatus, @Param("startDate") String startDate,
			@Param("endDate") String endDate, @Param("minValue") Long minValue, @Param("maxValue") Long maxValue);

	void deleteById(Long id);

	boolean existsByDepositCardNetworkProviderAndSerialAndCodeAndValue(
			DepositCardNetworkProvider depositCardNetworkProvider, String serial, String code, long value);

	long count();
}
