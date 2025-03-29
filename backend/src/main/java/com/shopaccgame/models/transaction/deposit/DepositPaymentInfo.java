package com.shopaccgame.models.transaction.deposit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DepositPaymentInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private int viettelTradecost;
	@Column(nullable = false)
	private int mobifoneTradecost;
	@Column(nullable = false)
	private int vinaphoneTradecost;
	@Column(nullable = false)
	private String qrCodeMomo = "";
	@Column(nullable = false)
	private String qrCodeViettelPay = "";
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getViettelTradecost() {
		return viettelTradecost;
	}
	public void setViettelTradecost(int viettelTradecost) {
		this.viettelTradecost = viettelTradecost;
	}
	public int getMobifoneTradecost() {
		return mobifoneTradecost;
	}
	public void setMobifoneTradecost(int mobifoneTradecost) {
		this.mobifoneTradecost = mobifoneTradecost;
	}
	public int getVinaphoneTradecost() {
		return vinaphoneTradecost;
	}
	public void setVinaphoneTradecost(int vinaphoneTradecost) {
		this.vinaphoneTradecost = vinaphoneTradecost;
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
