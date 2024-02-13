package com.hti.smpp.common.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.sales.dto.SalesEntry;
import com.hti.smpp.common.user.dto.User;

@Repository
public interface SalesRepository extends JpaRepository<SalesEntry, Integer> {

	public SalesEntry findByMasterId(String systemId);
	
	public SalesEntry findByUsername(String username);

	public List<SalesEntry> findByMasterIdAndRole(String mgrId, String role);

	public List<SalesEntry> findByRole(String role);

	@Query("SELECT new com.hti.smpp.common.user.dto.User(s.id, s.username, s.password, s.role) FROM SalesEntry s WHERE s.username = :username")
	public Optional<User> getUsers(@Param("username") String username);

	public boolean existsByUsername(String username);

}