package com.hti.smpp.common.twoway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.twoway.dto.ReportEntry;

@Repository
public interface ReportEntryRepository extends JpaRepository<ReportEntry, Integer> {

}
