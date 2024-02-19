package com.hti.smpp.common.user.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.User;
import com.hti.smpp.common.user.dto.UserEntry;

@Repository
public interface UserEntryRepository extends JpaRepository<UserEntry, Integer> {

	public Optional<UserEntry> findBySystemId(String system_id);

	@Query("SELECT new com.hti.smpp.common.user.dto.User(u.id, u.systemId, u.password, u.role) FROM UserEntry u WHERE u.systemId = :systemId")
	public Optional<User> getUsers(@Param("systemId") String systemId);

	public boolean existsBySystemId(String username);
	
	public UserEntry findByRole(String role);
	
	@Query("SELECT u.id FROM UserEntry u")
    Set<Integer> getAllIds();
}
