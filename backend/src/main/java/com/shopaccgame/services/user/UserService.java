package com.shopaccgame.services.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopaccgame.dtos.user.UserDTO;
import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.exceptions.user.UpdateUserStatusException;
import com.shopaccgame.exceptions.user.UserNotFoundException;
import com.shopaccgame.models.user.User;
import com.shopaccgame.repositories.user.UserRepository;
import com.shopaccgame.utils.DTOConvertToEntityUtil;
import com.shopaccgame.utils.EntityConvertToDTOUtil;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public UserDTO getUserById(Long id) {
		User user = userRepository.findById(id).orElseThrow(
				() -> new UserNotFoundException("Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND));
		return EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
	}

	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new UserNotFoundException("Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND));
		return EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
	}

	public UserDTO getUserByProviderAndProviderId(String provider, String providerId) {
		User user = userRepository.findByProviderAndProviderId(provider, providerId).orElseThrow(
				() -> new UserNotFoundException("Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND));
		return EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
	}

	public List<UserDTO> getUsersByRole(UserRole role) {
		List<User> users = userRepository.findByRole(role);
		return EntityConvertToDTOUtil.convertToListDTO(users, UserDTO.class);
	}

	public Page<User> getAllUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	public Page<User> getUsersFollowFilter(Pageable pageable, UserStatus userStatus, UserRole role) {
		if (userStatus != null && role != null) {
			return userRepository.findByUserStatusAndRole(userStatus, role, pageable);
		} else if (userStatus != null) {
			return userRepository.findByUserStatus(userStatus, pageable);
		} else if (role != null) {
			return userRepository.findByRole(role, pageable);
		} else {
			return userRepository.findAll(pageable);
		}
	}

	public boolean getStatusUser(User user) {
		return user.getUserStatus() == UserStatus.ACTIVE;
	}

	public long getCountUsers() {
		return userRepository.count();
	}

	@Transactional
	public UserDTO updateUser(Long userId, UserDTO userDTO) {
		User existingUser = userRepository.findById(userId).orElseThrow(
				() -> new UpdateUserStatusException("Không tìm được ID tài khoản cần cập nhật.", HttpStatus.NOT_FOUND));

		User user = DTOConvertToEntityUtil.convertToEntity(userDTO, User.class);
		user.setId(userId);

		user.setUsername(existingUser.getUsername());
		user.setEmail(existingUser.getEmail());

		User updatedUser = userRepository.save(user);
		return EntityConvertToDTOUtil.convertToDTO(updatedUser, UserDTO.class);
	}

	@Transactional
	public UserDTO updateStatusUser(Long userId, UserStatus userStatus) {

		User user = userRepository.findById(userId).orElseThrow(() -> {
			return new UpdateUserStatusException("Không tìm được ID tài khoản cần khóa.", HttpStatus.NOT_FOUND);
		});

		user.setUserStatus(userStatus);

		return EntityConvertToDTOUtil.convertToDTO(user, UserDTO.class);
	}

	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}
}