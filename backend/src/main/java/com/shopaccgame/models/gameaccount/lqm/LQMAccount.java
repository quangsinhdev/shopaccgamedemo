package com.shopaccgame.models.gameaccount.lqm;

import com.shopaccgame.models.gameaccount.GameAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class LQMAccount extends GameAccount {
	@Column(nullable = false)
	private int champ = 0;
	@Column(nullable = false)
	private int skin = 0;
	@Column(name = "`rank`", nullable = false)
	private String rank = "";

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
