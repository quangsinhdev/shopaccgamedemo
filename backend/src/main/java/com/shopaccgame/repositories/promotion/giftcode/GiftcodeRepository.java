package com.shopaccgame.repositories.promotion.giftcode;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.models.promotion.giftcode.Giftcode;

@Repository
public interface GiftcodeRepository extends JpaRepository<Giftcode, Long> {
	Page<Giftcode> findAll(Pageable pageable);

	Optional<Giftcode> findById(Long id);

	Optional<Giftcode> findByCode(String code);

	boolean existsByCode(String code);
	
	void deleteById(Long id);

	long count();
}
