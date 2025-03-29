package com.shopaccgame.dtos.transaction.deposit;

public class DepositPaymentInfoDTO {
	private int viettelTradeCost;
	private int mobifoneTradeCost;
	private int vinaphoneTradeCost;
	private String qrCodeMomo;
	private String qrCodeViettelPay;

	public int getViettelTradeCost() {
		return viettelTradeCost;
	}

	public void setViettelTradeCost(int viettelTradeCost) {
		this.viettelTradeCost = viettelTradeCost;
	}

	public int getMobifoneTradeCost() {
		return mobifoneTradeCost;
	}

	public void setMobifoneTradeCost(int mobifoneTradeCost) {
		this.mobifoneTradeCost = mobifoneTradeCost;
	}

	public int getVinaphoneTradeCost() {
		return vinaphoneTradeCost;
	}

	public void setVinaphoneTradeCost(int vinaphoneTradeCost) {
		this.vinaphoneTradeCost = vinaphoneTradeCost;
	}

	public String getQrCodeMomo() {
		return qrCodeMomo;
	}

	public void setQrCodeMomo(String qrCodeMomo) {
		this.qrCodeMomo = qrCodeMomo;
	}

	public String getQrCodeViettelPay() {
		return qrCodeViettelPay;
	}

	public void setQrCodeViettelPay(String qrCodeViettelPay) {
		this.qrCodeViettelPay = qrCodeViettelPay;
	}

}
