package com.shopaccgame.dtos.user;

import java.time.LocalDateTime;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class UserDTO {
	private Long id;
	private String fullname;
	private String username;
	private String email;
	@Enumerated(EnumType.STRING)
	private UserRole role;
	@Enumerated(EnumType.STRING)
	private UserStatus userStatus;
	private long balance;
	private long totaldeposit;
	private LocalDateTime TimeCreateAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public long getTotaldeposit() {
		return totaldeposit;
	}

	public void setTotaldeposit(long totaldeposit) {
		this.totaldeposit = totaldeposit;
	}

	public LocalDateTime getTimeCreateAt() {
		return TimeCreateAt;
	}

	public void setTimeCreateAt(LocalDateTime timeCreateAt) {
		TimeCreateAt = timeCreateAt;
	}
}
