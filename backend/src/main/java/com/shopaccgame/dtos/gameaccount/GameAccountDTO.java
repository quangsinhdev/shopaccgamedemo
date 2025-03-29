package com.shopaccgame.dtos.gameaccount;

import java.util.List;

import com.shopaccgame.enums.gameaccount.GameAccountType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class GameAccountDTO {
	private Long id;

	@Enumerated(EnumType.STRING)
	private GameAccountType gameAccountType;

	private long price;
	private String description;
	private int discount;
	private List<String> imagesAsList;

	private long valueteam;
	private long bp;
	private int fc;
	private int tinhhoalam;
	private int rp;
	private int champ;
	private int skin;
	private String rank;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GameAccountType getGameAccountType() {
		return gameAccountType;
	}

	public void setGameAccountType(GameAccountType gameAccountType) {
		this.gameAccountType = gameAccountType;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public List<String> getImagesAsList() {
		return imagesAsList;
	}

	public void setImagesAsList(List<String> imagesAsList) {
		this.imagesAsList = imagesAsList;
	}

	public long getValueteam() {
		return valueteam;
	}

	public void setValueteam(long valueteam) {
		this.valueteam = valueteam;
	}

	public long getBp() {
		return bp;
	}

	public void setBp(long bp) {
		this.bp = bp;
	}

	public int getFc() {
		return fc;
	}

	public void setFc(int fc) {
		this.fc = fc;
	}

	public int getTinhhoalam() {
		return tinhhoalam;
	}

	public void setTinhhoalam(int tinhhoalam) {
		this.tinhhoalam = tinhhoalam;
	}

	public int getRp() {
		return rp;
	}

	public void setRp(int rp) {
		this.rp = rp;
	}

	public int getChamp() {
		return champ;
	}

	public void setChamp(int champ) {
		this.champ = champ;
	}

	public int getSkin() {
		return skin;
	}

	public void setSkin(int skin) {
		this.skin = skin;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}
}