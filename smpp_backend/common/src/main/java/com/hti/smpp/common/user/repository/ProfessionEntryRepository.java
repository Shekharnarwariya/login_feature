package com.hti.smpp.common.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.ProfessionEntry;

@Repository
public interface ProfessionEntryRepository extends JpaRepository<ProfessionEntry, Integer> {

	public boolean existsByDomainEmail(String email);

	public Optional<ProfessionEntry> findByUserId(int userId);

	@Query(value = "SELECT * FROM profession_info WHERE domain_email IS NOT NULL", nativeQuery = true)
	public List<ProfessionEntry> findByDomainEmailIsNotNull();

}
