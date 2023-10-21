package com.hti.smpp.common.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.report.dto.ProfitReportEntry;

@Repository
public interface ProfitReportEntryRepository extends JpaRepository<ProfitReportEntry, Integer> {

}
