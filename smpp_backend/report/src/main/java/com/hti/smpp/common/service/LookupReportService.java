package com.hti.smpp.common.service;

import java.util.List;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.rmi.dto.LookupReport;

import jakarta.servlet.http.HttpServletResponse;

public interface LookupReportService {

	public List<LookupReport> LookupReportview(String username, CustomReportForm customReportForm, String lang);

	public String LookupReportxls(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String LookupReportPdf(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);

	public String LookupReportDoc(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);
	

	public String LookupReportRecheck(String username, CustomReportForm customReportForm, String lang,
			HttpServletResponse response);
	

}
