package com.shopaccgame.repositories.promotion.voucher;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.promotion.voucher.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
	Page<Voucher> findAll(Pageable pageable);
	Optional<Voucher> findByCode(String code);

	Optional<Voucher> findById(Long id);
	
	boolean existsByCode(String code);

	void deleteById(Long id);

	long count();
}
