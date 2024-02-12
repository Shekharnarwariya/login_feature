package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.report.dto.ProfitReportEntry;
import com.hti.smpp.common.report.dto.ReportCriteria;

public interface ReportDAO {
	
	public List<ProfitReportEntry> listProfitReport(ReportCriteria rc);

}
