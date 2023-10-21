package com.hti.smpp.common.smsc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.smsc.dto.SmscEntry;

@Repository
public interface SmscEntryRepository extends JpaRepository<SmscEntry, Integer> {

	public List<SmscEntry> findByMasterId(String masterId);

}
