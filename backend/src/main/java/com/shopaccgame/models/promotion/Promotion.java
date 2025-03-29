package com.shopaccgame.models.promotion;

import java.time.LocalDateTime;

import com.shopaccgame.enums.promotion.PromotionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Promotion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String code;

	@Column(nullable = false)
	private long value = 0;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private PromotionStatus promotionStatus;

	@Column(name = "Time_of_listing", nullable = false)
	private LocalDateTime timeOfListing = LocalDateTime.now();

	@Column(name = "Time_of_use", nullable = false)
	private LocalDateTime timeOfUse = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public PromotionStatus getPromotionStatus() {
		return promotionStatus;
	}

	public void setPromotionStatus(PromotionStatus promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public LocalDateTime getTimeOfListing() {
		return timeOfListing;
	}

	public void setTimeOfListing(LocalDateTime timeOfListing) {
		this.timeOfListing = timeOfListing;
	}

	public LocalDateTime getTimeOfUse() {
		return timeOfUse;
	}

	public void setTimeOfUse(LocalDateTime timeOfUse) {
		this.timeOfUse = timeOfUse;
	}

}
