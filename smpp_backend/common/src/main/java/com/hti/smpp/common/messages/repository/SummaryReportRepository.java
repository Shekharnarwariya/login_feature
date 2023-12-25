package com.hti.smpp.common.messages.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hti.smpp.common.messages.dto.SummaryReport;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, Integer> {

	 /**
     * Retrieves distinct content and date from the summary_report table for a specific username.
     *
     * @param username the username to filter results
     * @return a list of Object arrays representing distinct content and date
     */
	
	@Query(value = "SELECT DISTINCT s.content, s.date FROM summary_report s WHERE s.username = :username ORDER BY s.date DESC", nativeQuery = true)
	public List<Object[]> getRecentContent(@Param("username") String username);
	  /**
     * Retrieves distinct content and date from the summary_report table for a specific username
     * with an additional search filter on content.
     *
     * @param username      the username to filter results
     * @param searchKeyword the keyword to search for in the content
     * @return a list of Object arrays representing distinct content and date
     */
	@Query(value = "SELECT DISTINCT s.content, s.date FROM summary_report s WHERE s.username = :username AND s.content LIKE %:searchKeyword% ORDER BY s.date DESC", nativeQuery = true)
	public List<Object[]> getRecentContentWithSearch(@Param("username") String username,
			@Param("searchKeyword") String searchKeyword);

}
