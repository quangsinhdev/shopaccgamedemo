package com.shopaccgame.repositories.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopaccgame.enums.user.UserRole;
import com.shopaccgame.enums.user.UserStatus;
import com.shopaccgame.models.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	User save(User user);

	Optional<User> findById(Long id);

	Optional<User> findByUsername(String username);

	Optional<User> findByProviderAndProviderId(String provider, String providerId);

	List<User> findByRole(UserRole role);

	Page<User> findAll(Pageable pageable);
	
	Page<User> findByUserStatus(UserStatus userStatus, Pageable pageable);
	
	Page<User> findByRole(UserRole role, Pageable pageable);
	
	Page<User> findByUserStatusAndRole(UserStatus userStatus, UserRole role, Pageable pageable);

	void deleteById(Long id);

	boolean existsById(Long id);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
	

	long count();
}
