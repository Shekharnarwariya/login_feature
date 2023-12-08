package com.hti.smpp.common.twoway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.twoway.dto.KeywordEntry;

@Repository
public interface KeywordEntryRepository extends JpaRepository<KeywordEntry, Integer> {
	public  List<KeywordEntry> findByUserIdIn(Integer[] users);
}
