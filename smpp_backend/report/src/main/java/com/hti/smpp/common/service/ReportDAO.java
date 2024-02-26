package com.hti.smpp.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hti.smpp.common.report.dto.ProfitReportEntry;
import com.hti.smpp.common.report.dto.ReportCriteria;

public interface ReportDAO {
	
	public Page<ProfitReportEntry> listProfitReport(ReportCriteria rc, Pageable pageable);

}
