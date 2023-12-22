package com.hti.smpp.common.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.ProfessionEntry;

@Repository
public interface ProfessionEntryRepository extends JpaRepository<ProfessionEntry, Integer> {

	public boolean existsByDomainEmail(String email);

	public Optional<ProfessionEntry> findByUserId(int userId);

}
