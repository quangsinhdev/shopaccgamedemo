package com.shopaccgame.models.gameaccount;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.enums.gameaccount.GameAccountType;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@MappedSuperclass
public abstract class GameAccount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "game_account_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private GameAccountType gameAccountType;

	@Column(nullable = false)
	private long price = 0;
	@Column(nullable = false, unique = true)
	private String username;
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String phonenumber = "";
	@Column(nullable = false, unique = true)
	private String email = "";
	@Column(nullable = false)
	private String description = "";
	@Column(nullable = false)
	private int discount = 0;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GameAccountStatus gameAccountStatus;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String images = "";

	@Column(name = "Time_of_Listing", nullable = false)
	private LocalDateTime timeOfListing = LocalDateTime.now();

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

	public String getImages() {
		return images;
	}

	public void setImages(List<String> imageList) {
	    setImagesFromList(imageList);
	}
	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public GameAccountStatus getGameAccountStatus() {
		return gameAccountStatus;
	}

	public void setGameAccountStatus(GameAccountStatus gameAccountStatus) {
		this.gameAccountStatus = gameAccountStatus;
	}

	public void setImagesFromList(List<String> imageList) {
	    this.images = (imageList != null && !imageList.isEmpty()) ? String.join(",", imageList) : "";
	}

	public List<String> getImagesAsList() {
	    return images.isEmpty() ? Collections.emptyList() : Arrays.asList(images.split(","));
	}

	public LocalDateTime getTimeOfListing() {
		return timeOfListing;
	}

	public void setTimeOfListing(LocalDateTime timeOfListing) {
		this.timeOfListing = timeOfListing;
	}
}
