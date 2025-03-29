package com.shopaccgame.repositories.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.models.user.User;

@Repository
public interface AdminRepository extends JpaRepository<User, Long> {
	Page<User> findAll(Pageable pageable);

	List<User> findByRole(UserRole role);

	List<User> findByUsername(String username);

	void deleteById(Long id);

	void delete(User user);

	long count();

	@Modifying
	@Query("UPDATE User u SET u.userStatus = :userStatus WHERE u.id = :id")
	void updateStatus(Long id, UserStatus userStatus);

	@Modifying
	@Query("UPDATE User u SET u.role = :role WHERE u.id = :id")
	void updateRoleUser(Long id, UserRole role);
}
