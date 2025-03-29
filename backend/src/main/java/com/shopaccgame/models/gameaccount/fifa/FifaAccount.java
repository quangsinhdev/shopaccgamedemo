package com.shopaccgame.models.gameaccount.fifa;

import com.shopaccgame.models.gameaccount.GameAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class FifaAccount extends GameAccount {
	@Column(nullable = false)
	private long valueteam = 0;
	@Column(nullable = false)
	private long bp = 0;
	@Column(nullable = false)
	private int fc = 0;

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

}
