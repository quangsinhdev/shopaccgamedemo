package com.shopaccgame.dtos.promotion.giftcode;

public class GiftcodeResponseDTO {
	private long value;
	private String messageResponse;

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getMessageResponse() {
		return messageResponse;
	}

	public void setMessageResponse(String messageResponse) {
		this.messageResponse = messageResponse;
	}
}
