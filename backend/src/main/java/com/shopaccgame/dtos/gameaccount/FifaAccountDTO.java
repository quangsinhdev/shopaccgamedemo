package com.shopaccgame.dtos.gameaccount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.shopaccgame.enums.gameaccount.GameAccountStatus;
import com.shopaccgame.enums.gameaccount.GameAccountType;
import com.shopaccgame.validators.EnumValid;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class FifaAccountDTO {
	private Long id;

	@NotNull(message = "Thể loại Game của tài khoản không được bỏ trống.")
	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = GameAccountType.class, message = "Thể loại Game của tài khoản không hợp lệ.")
	private GameAccountType gameAccountType;

	@Min(value = 0, message = "Giá tiền tài khoản phải lớn hơn bằng 0")
	private long price;

	@NotBlank(message = "Tài khoản của account không được bỏ trống")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tài khoản của account không đúng")
	private String username;

	@NotBlank(message = "Mật khẩu của account không được bỏ trống")
	@Size(min = 6, max = 100, message = "Mật khẩu của account không đúng. Vui lòng kiểm tra lại.")
	private String password;

	@Pattern(regexp = "\\d*", message = "Số điện thoại chỉ có thể chứa số.")
	private String phonenumber = "";

	@Email(message = "Email của tài khoản không đúng. Vui lòng kiểm tra lại.")
	private String email = "";

	@Pattern(regexp = "^[a-zA-Zàáảãạăắằẳẵặâấầẩẫậđéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵ0-9.,\\s]*$", message = "Mô tả tài khoản chỉ có thể chứa chữ cái , số và dấu , .")
	private String description = "";

	@Min(value = 0, message = "Giảm giá không được nhỏ hơn 0%")
	@Max(value = 100, message = "Giảm giá không được vượt quá 100%")
	private int discount;

	@Enumerated(EnumType.STRING)
	@EnumValid(enumClass = GameAccountStatus.class, message = "Tình trạng tài khoản Game không hợp lệ")
	private GameAccountStatus gameAccountStatus;

	@Size(min = 0, message = "Danh sách hình ảnh không hợp lệ.")
	private List<@Pattern(regexp = "^https?://[a-zA-Z0-9._/:=-]+$", message = "URL Images phải là URL hợp lệ bắt đầu bằng http hoặc https") String> imagesAsList;
	@Min(value = 0, message = "Giá trị đội hình không được nhỏ hơn 0!")
	private long valueteam;

	@Min(value = 0, message = "Số BP không được nhỏ hơn 0!")
	private long bp;

	@Min(value = 0, message = "Số FC không được nhỏ hơn 0!")
	private int fc;

	@NotNull(message = "Thời điểm đăng bán tài khoản không được bỏ trống")
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

	public List<String> getImagesAsList() {
		return imagesAsList;
	}

	public void setImagesAsList(List<String> imagesAsList) {
		this.imagesAsList = (imagesAsList == null || imagesAsList.isEmpty()) ? new ArrayList<>() : imagesAsList;
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

	public LocalDateTime getTimeOfListing() {
		return timeOfListing;
	}

	public void setTimeOfListing(LocalDateTime timeOfListing) {
		this.timeOfListing = timeOfListing;
	}
}
