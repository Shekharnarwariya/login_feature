package com.hti.smpp.common.bsfm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.bsfm.dto.Bsfm;

import jakarta.transaction.Transactional;
/**
 * Repository interface for Bsfm profiles, extending JpaRepository for CRUD operations.
 */
@Repository
public interface BsfmProfileRepository extends JpaRepository<Bsfm, Integer> {
	@Query("SELECT MAX(b.priority) FROM Bsfm b")
	public Integer findMaxPriority();

	public List<Bsfm> findByMasterIdOrderByPriority(String masterId);

	public Bsfm findByUsername(String username);

	public Bsfm findByProfilename(@Param("profilename") String profilename);

	@Transactional
	public long deleteByProfilename(String profileName);
}