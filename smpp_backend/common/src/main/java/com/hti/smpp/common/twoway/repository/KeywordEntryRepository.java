package com.hti.smpp.common.twoway.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.twoway.dto.KeywordEntry;

@Repository
public interface KeywordEntryRepository extends JpaRepository<KeywordEntry, Integer> {
	Page<KeywordEntry> findByUserIdIn(Integer[] users, Pageable p);
	
	@Query("SELECT k FROM KeywordEntry k WHERE k.createdOn BETWEEN :start AND :end")
	Page<KeywordEntry> searchByDate(@Param("start") String start, @Param("end") String end,Pageable p);
	
	@Query("SELECT k FROM KeywordEntry k WHERE k.shortCode LIKE CONCAT('%',:search,'%') OR k.type LIKE CONCAT('%',:search,'%') OR k.prefix LIKE CONCAT('%',:search,'%') OR k.suffix LIKE CONCAT('%',:search,'%') OR k.createdBy LIKE CONCAT('%',:search,'%')")
	Page<KeywordEntry> searchKeyword(@Param("search") String search, Pageable p);
	
	@Query("SELECT k FROM KeywordEntry k WHERE (k.shortCode LIKE CONCAT('%',:search,'%') OR k.type LIKE CONCAT('%',:search,'%') OR k.prefix LIKE CONCAT('%',:search,'%') OR k.suffix LIKE CONCAT('%',:search,'%') OR k.createdBy LIKE CONCAT('%',:search,'%')) AND k.userId IN :users")
	Page<KeywordEntry> searchKeywordAndFindByUserIdIn(@Param("search") String search, @Param("users") Integer[] users, Pageable p);
	
	@Query("SELECT k FROM KeywordEntry k WHERE (k.createdOn BETWEEN :start AND :end) AND k.userId IN :users")
	Page<KeywordEntry> searchByDateAndFindByUserIdIn(@Param("start") String start, @Param("end") String end, @Param("users") Integer[] users,Pageable p);
}
