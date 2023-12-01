package com.hti.smpp.common.login.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.login.dto.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findBySystemId(String username);
				
	Boolean existsBySystemId(String username);

	Boolean existsByEmail(String email);

	public Optional<User> findByEmail(String email);
}
