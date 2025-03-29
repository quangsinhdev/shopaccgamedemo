package com.shopaccgame.models.promotion.giftcode;

import com.shopaccgame.models.promotion.Promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Giftcode extends Promotion {
	@Column(nullable = false)
	private String giftcodeInfo = "";

	public String getGiftcodeInfo() {
		return giftcodeInfo;
	}

	public void setGiftcodeInfo(String giftcodeInfo) {
		this.giftcodeInfo = giftcodeInfo;
	}

}
