package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.BatchDTO;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface SummaryReportService {
	
	public List<BatchDTO> SummaryReportview(String username, CustomReportForm customReportForm, String lang);
	
	public String  SummaryReportxls(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	public String  SummaryReportpdf(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	public String  SummaryReportdoc(String username, CustomReportForm customReportForm,
			HttpServletResponse response,String lang);
	
	

}
