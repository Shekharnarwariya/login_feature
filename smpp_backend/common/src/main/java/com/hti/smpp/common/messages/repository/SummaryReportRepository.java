package com.hti.smpp.common.messages.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hti.smpp.common.messages.dto.SummaryReport;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, Integer> {

	@Query(value = "SELECT DISTINCT s.content FROM summary_report s WHERE s.username = :username ORDER BY s.date DESC", nativeQuery = true)
	public Set<String> getRecentContent(@Param("username") String username);
	
	@Query(value = "SELECT DISTINCT s.content FROM summary_report s WHERE s.username = :username AND s.content LIKE %:searchKeyword% ORDER BY s.date DESC", nativeQuery = true)
	public Set<String> getRecentContentWithSearch(@Param("username") String username, @Param("searchKeyword") String searchKeyword);

}
