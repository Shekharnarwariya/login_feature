package com.hti.smpp.common.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.WebMasterEntry;

@Repository
public interface WebMasterEntryRepository extends JpaRepository<WebMasterEntry, Integer> {

	public WebMasterEntry findByUserId(int system_id);

	public List<WebMasterEntry> findByAutoCopyRouting(boolean autoCopyRouting);

	public List<WebMasterEntry> findBySecondaryMaster(String systemid);

	@Query("SELECT COUNT(u) FROM WebMasterEntry u WHERE u.executiveId = :sellerId")
	public long countUsersUnderSeller(int sellerId);

	@Query("SELECT e FROM WebMasterEntry e WHERE e.minFlag = :minFlag")
	public List<WebMasterEntry> findByMinFlag(@Param("minFlag") boolean minFlag);

	@Query("SELECT e FROM WebMasterEntry e WHERE e.email IS NOT NULL AND e.email LIKE '%@%' AND e.email LIKE '%.%' AND e.misReport = TRUE")
	public List<WebMasterEntry> findValidEntries();

	@Query("SELECT w FROM WebMasterEntry w WHERE w.coverageReport <> 'No' AND w.coverageEmail IS NOT NULL AND w.coverageEmail LIKE '%@%.%'")
	public List<WebMasterEntry> findAllWithCoverageReportAndEmail();

	public List<WebMasterEntry> findByExecutiveId(int executiveId);
}
