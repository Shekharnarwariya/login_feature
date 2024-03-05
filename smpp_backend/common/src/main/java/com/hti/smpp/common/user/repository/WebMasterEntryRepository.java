package com.hti.smpp.common.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.WebMasterEntry;

@Repository
public interface WebMasterEntryRepository extends JpaRepository<WebMasterEntry, Integer> {

	public WebMasterEntry findByUserId(int system_id);

	public List<WebMasterEntry> findByAutoCopyRouting(boolean autoCopyRouting);

	public List<WebMasterEntry> findBySecondaryMaster(String systemid);

	@Query("SELECT COUNT(u) FROM WebMasterEntry u WHERE u.executiveId = :sellerId")
	public long countUsersUnderSeller(int sellerId);

	public List<WebMasterEntry> findByMinFlag(boolean minFlag);
}
